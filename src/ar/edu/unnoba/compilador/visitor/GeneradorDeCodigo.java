package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.OperacionBinariaAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Conjuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Disyuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import jflex.base.Pair;

import java.io.*;
import java.util.*;

/* Clase para genera código de LLVM IR a partir del AST */

public class GeneradorDeCodigo extends Visitor {
    // Sobre «codigo» vamos anexando lo que va a ser el resultado final en IR
    private StringBuilder codigo;

    private String nombreArchivoFuente;

    // Para imprimir o no los comentarios del recorrido.
    private Boolean comentariosOn;

    // Tabla con las funciones definidas por el usuario. Se extrae del objeto Programa al entrar en esta clase
    private Map<String, SimboloFuncion> tablaFunciones;

    // Lista para guardar los nombres de las funciones que inicializan a las variables globales
    // Se añaden en el visit(DecVarIni), y se procesa una vez que se alcanza el main
    private final List<String> varGblInit = new ArrayList<>();

    private List<Cadena> arrCadenas;

    /* Pila de pares de etiquetas para saber a donde saltar cuando se encuentra break o continue
     * dentro de un while.
     * La primer etiqueta del par representa el comienzo del bloque si la condición es verdadera,
     * y la segunda si es falsa (o el fin del bloque).
     */
    private final Deque<Pair<String, String>> etiquetasMientras = new ArrayDeque<>();

    /* Pila de pares de etiquetas usada para el cortocircuito booleano */
    private final Deque<Pair<String, String>> etiquetasOpBinLog = new ArrayDeque<>();

    public static boolean targetEsWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /*** Funciones auxiliares ***/

    /* Función para no tener fija la arquitectura y SO destino.
     * El triple se puede sacar de  "llvm-config --host-target"
     * o "clang -print-target-triple", pero el datalayout no se.
     * Compilar un programa básico en C a IR y fijarse.
     */
    private static String getHostTarget() throws IOException, InterruptedException {
        String datalayout = null;
        String triple = null;

        PrintWriter pw = new PrintWriter(new FileWriter("void.c"));
        pw.println("int main() {}");
        pw.close();
        Process clang = Runtime.getRuntime().exec("clang -emit-llvm -S -o - void.c");
        BufferedReader reader = new BufferedReader(new InputStreamReader(clang.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null && (datalayout == null || triple == null)) {
            if (line.startsWith("target datalayout")) {
                datalayout = line;
            } else if (line.startsWith("target triple")) {
                triple = line;
            }
        }
        clang.waitFor();
        // borrar archivo C temporal
        File file = new File("void.c");
        file.delete();
        if (datalayout == null || triple == null) {
            // falló algo... tirar excepción para usar datos predeterminados
            throw new IOException("No se pudieron obtener los datos del host");
        }
        return datalayout + "\n" + triple;
    }

    private void imprimirEtiqueta(String nombreEtiqueta) {
        codigo.append(String.format("\n%s:\n", nombreEtiqueta));
    }

    /* Agregar una línea de código indentado */
    private void imprimirCodigo(String codigo) {
        this.codigo.append("\t").append(codigo).append("\n");
    }

    /* Generar código con un comentarios alineados, si es posible */
    private void imprimirCodigo(String codigo, String comentario) {
        int cantEspacios = 70 - codigo.length();

        if (cantEspacios < 1) cantEspacios = 1;

        String espacios = " ".repeat(cantEspacios);
        imprimirCodigo(codigo + espacios + "; " + comentario);
    }

    private void imprimirCodSaltoInc(String etiquetaDestino) {
        imprimirCodigo(String.format("br label %%%s", etiquetaDestino));
    }

    private void imprimirCodSaltoCond(String cond, String etiquetaTrue, String etiquetaFalse) {
        imprimirCodigo(String.format("br i1 %s, label %%%s, label %%%s",
                cond, etiquetaTrue, etiquetaFalse));
    }

    /* Si se pidió mostrar comentarios para depurar la generación de código, agregar un comentario */
    private void imprimirComent(String comentario) {
        if (comentariosOn) {
            codigo.append("\n");
            imprimirCodigo(String.format("; %s", comentario));
        }
    }


    /* Para tratar los casos de invocaciones a write */
    private void imprimirWrite(InvocacionFuncion i) throws ExcepcionVisitor {
        Expresion arg = i.getArgs().get(0);
        arg.accept(this);
        String refIR = arg.getRefIR();

        String paramsPrintf;

        if (arg instanceof Cadena) {
            Cadena cad = (Cadena) arg;
            paramsPrintf = grarParamsPrintf(cad.getLongitudIR(), cad.getNombreIR());

            imprimirCodigo(String.format("%s = call i32 (i8*, ...) @printf(%s)",
                    Normalizador.crearNomRef("aux"), paramsPrintf));
        } else if (arg.getTipo().equals(Tipo.BOOLEAN)) {
            // FIXME: Tendría que ser así directamente, pero por algún motivo imprime cualquier cosa en Windows
            //paramsPrintf = grarParamsPrintfConRef(3, "@.int_format", "i1", refIR);

            // Extiendo el i1 a i32 para poder imprimirlo
            String refExt = Normalizador.crearNomRef("ext");
            imprimirCodigo(String.format("%s = zext i1 %s to i32", refExt, refIR));
            refIR = refExt;

            // Ahora se evalúa el valor del booleano para imprimir "true" o "false".
            String casoTrue = Normalizador.crearNomEtiqueta("bln_true");
            String casoFalse = Normalizador.crearNomEtiqueta("bln_false");
            String fin = Normalizador.crearNomEtiqueta("fin_eval_bln");

            String refAux = Normalizador.crearNomRef("aux");

            // Guardo en refAux el valor de la comparación
            imprimirCodigo(String.format("%s = icmp eq i32 %s, 1", refAux, refIR));
            imprimirCodSaltoCond(refAux, casoTrue, casoFalse);

            imprimirEtiqueta(casoTrue);
            imprimirCodigo(String.format("%s = call i32 (i8*, ...) @printf(%s)",
                    Normalizador.crearNomRef("aux"),
                    grarParamsPrintfConRef(5, "@.bln_true_format", "i32", refIR)));
            imprimirCodSaltoInc(fin);

            imprimirEtiqueta(casoFalse);
            imprimirCodigo(String.format("%s = call i32 (i8*, ...) @printf(%s)",
                    Normalizador.crearNomRef("aux"),
                    grarParamsPrintfConRef(6, "@.bln_false_format", "i32", refIR)));
            imprimirCodSaltoInc(fin);

            imprimirEtiqueta(fin);
        } else {
            // Es un número entero o flotante
            String tipoIR = arg.getTipo().equals(Tipo.INTEGER) ? "i32" : "double";

            if (arg.getTipo().equals(Tipo.INTEGER)) {
                paramsPrintf = grarParamsPrintfConRef(3, "@.int_format", tipoIR, refIR);
            } else {
                paramsPrintf = grarParamsPrintfConRef(6, "@.double_print_format", tipoIR, refIR);
            }

            imprimirCodigo(String.format("%s = call i32 (i8*, ...) @printf(%s)",
                    Normalizador.crearNomRef("aux"), paramsPrintf));
        }

        if (i.getNombre().equals("writeln")) {
            // Genero un salto de línea
            paramsPrintf = grarParamsPrintf(2, "@.salto_linea");
            imprimirCodigo(String.format("%s = call i32 (i8*, ...) @printf(%s)",
                    Normalizador.crearNomRef("aux"), paramsPrintf));
        }
    }

    private String grarParamsPrintf(int tamanho, String str) {
        return String.format("i8* getelementptr([%1$s x i8], [%1$s x i8]* %2$s, i32 0, i32 0)",
                tamanho, str);
    }

    private String grarParamsPrintfConRef(int tamanho, String str, String tipoIR, String refIR) {
        return String.format("i8* getelementptr([%1$s x i8], [%1$s x i8]* %2$s, i32 0, i32 0), %3$s %4$s",
                tamanho, str, tipoIR, refIR);
    }

    private void imprimirDefinicionLeer(Tipo tipo) {
        final String nombre = tipo.toString();
        final String tipoRet = tipo.getIR();
        final String tipoLeido;
        final String formatoScanf;
        if (tipo.equals(Tipo.FLOAT)) {
            // leer double para perder menos precisión
            tipoLeido = "double";
            formatoScanf = "([4 x i8], [4 x i8]* @.double_format";
        } else {
            // ya sea integer o boolean leer i32
            tipoLeido = "i32";
            formatoScanf = "([3 x i8], [3 x i8]* @.int_format";
        }

        codigo.append(String.format("define %s @read_%s() {\n", tipoRet, nombre));
        imprimirCodigo("%ptro.valor_leido = alloca " + tipoLeido);
        imprimirCodigo("%ref.ret_scanf = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds " +
                formatoScanf + ", i64 0, i64 0), " + tipoLeido + "* %ptro.valor_leido)");
        imprimirCodigo("%ref.valor_leido = load " + tipoLeido + ", " + tipoLeido +
                "* %ptro.valor_leido");

        if (tipo.equals(Tipo.BOOLEAN)) {
            // TODO: Mejorar. Ahora toma nro. impar = verdadero, cualquier otra cosa = falso
            imprimirCodigo("%ref.valor_convertido = trunc i32 %ref.valor_leido to i1");
            imprimirCodigo("ret " + tipoRet + " %ref.valor_convertido");
        } else {
            imprimirCodigo("ret " + tipoRet + " %ref.valor_leido");
        }

        codigo.append("}\n\n");
    }

    /* Devuelve una lista de parámetros de acuerdo a lo requerido por IR.
     * Utilizado para la declaración de funciones.
     */
    private String grarStrParams(List<Param> arrParams) {
        StringBuilder strParams = new StringBuilder();

        int cantParams = arrParams.size();
        for (int i = 0; i < cantParams; i++) {
            SimboloVariable simboloParam = (SimboloVariable) arrParams.get(i).getIdent();
            String tipoParam = simboloParam.getTipo().getIR();
            String paramNombreIR = simboloParam.getNombreIR();

            // Para separar los parámetros mediante comas, excepto el final
            String sep = i != cantParams - 1 ? ", " : "";

            // Añado el argumento a la lista
            strParams.append(String.format("%s %s%s", tipoParam, paramNombreIR, sep));
        }

        return strParams.toString();
    }

    private String grarStrArgs(List<Expresion> arrArgs) throws ExcepcionVisitor {
        // Similar a grarStrParams, pero esta lista es utilizada por las invocaciones, o sea el
        // argumento puede ser una variable, un literal o una expresión más compleja, mientras
        // que en la declaración de la función eso va a ser siempre un objeto de tipo Param.

        // FIXME: Parámetros por defecto

        StringBuilder strArgs = new StringBuilder();

        int cantArgs = arrArgs.size();

        for (int i = 0; i < cantArgs; i++) {
            Expresion exprArg = arrArgs.get(i);
            String tipoRetornoArg = exprArg.getTipo().getIR();
            // Evaluar la expr. y generar el refIR
            exprArg.accept(this);
            String argRefIR = exprArg.getRefIR();

            // Para separar los argumentos mediante comas, excepto el final
            String sep = i != cantArgs - 1 ? ", " : "";

            // Añado el argumento a la lista
            strArgs.append(String.format("%s %s%s", tipoRetornoArg, argRefIR, sep));
        }

        return strArgs.toString();
    }

    /*** Funciones auxiliares para generar el cortocircuito booleano ***/

    private void imprimirCortocircuito(OperacionBinariaLogica ob) throws ExcepcionVisitor {
        Pair<String, String> etiquetas = etiquetasOpBinLog.peek();
        assert etiquetas != null;
        String etiVerdadero = etiquetas.fst;
        String etiFalso = etiquetas.snd;

        imprimirComent(String.format("Cortocircuito booleano: %s", ob));

        Expresion expIzquierda = ob.getIzquierda();
        expIzquierda.accept(this);

        final String refIR = expIzquierda.getRefIR();
        final String etiTmp;

        if (ob instanceof Conjuncion) { // AND
            // Si el operador izquierdo es falso, esta operación es falsa
            etiTmp = Normalizador.crearNomEtiqueta("and_verdadero");
            if (expIzquierda instanceof NegacionLogica) {
                imprimirCodSaltoCond(refIR, etiFalso, etiTmp);
            } else {
                imprimirCodSaltoCond(refIR, etiTmp, etiFalso);
            }
        } else if (ob instanceof Disyuncion) { // OR
            // Si el operador izquierdo es verdadero, esta operación es verdadera
            etiTmp = Normalizador.crearNomEtiqueta("or_falso");
            if (expIzquierda instanceof NegacionLogica) {
                imprimirCodSaltoCond(refIR, etiTmp, etiVerdadero);
            } else {
                imprimirCodSaltoCond(refIR, etiVerdadero, etiTmp);
            }
        } else {
            throw new ExcepcionVisitor("Tipo de operación binaria lógica inesperado.");
        }

        imprimirEtiqueta(etiTmp);

        Expresion expDerecha = ob.getDerecha();
        expDerecha.accept(this);
        // asignar el nombre de la variable de la expresión derecha al resultado de la operación
        ob.setRefIR(expDerecha.getRefIR());
    }

    /* Generar y apilar etiquetas para asignaciones de expresiones binarias lógicas. */
    private void grarEtiCortocircuitoAsig(String etiqueta) {
        String etiVerdadero = Normalizador.crearNomEtiqueta(etiqueta + "_verdadero");
        String etiFalso = Normalizador.crearNomEtiqueta(etiqueta + "_falso");
        etiquetasOpBinLog.push(new Pair<>(etiVerdadero, etiFalso));
    }

    private void finalizarCortocircuitoAsig(String refIR, String nombreIR) {
        // Obtener y desapilar las etiquetas de esta asignación
        Pair<String, String> parEtiquetas = etiquetasOpBinLog.pop();

        String etiFin = Normalizador.crearNomEtiqueta("asig_fin");

        // Con el visit de expresión ya se generó el código para el cortocircuito.
        // Hay que agregar el último salto.
        imprimirCodSaltoCond(refIR, parEtiquetas.fst, parEtiquetas.snd);

        // Resultado verdadero
        imprimirEtiqueta(parEtiquetas.fst);
        imprimirCodigo(String.format("store i1 1, i1* %s", nombreIR));
        imprimirCodSaltoInc(etiFin);

        // Resultado falso
        imprimirEtiqueta(parEtiquetas.snd);
        imprimirCodigo(String.format("store i1 0, i1* %s", nombreIR));
        imprimirCodSaltoInc(etiFin);

        imprimirEtiqueta(etiFin);
    }

    /* Invoca a las funciones que asignan a las variables
     * globales el valor con el que fueron declaradas */
    private void inicializarVarsGbls() {
        varGblInit.forEach(fun -> imprimirCodigo(String.format("call void %s()", fun)));
    }

    /* Inicializa las cadenas que van a usarse en el programa
     */
    private void declararVarsStrs() {
        // Por ej.: @ptro.str.2 = private constant [5 x i8] c"Hola\00"
        arrCadenas.forEach(cad ->
                codigo.append(String.format("%s = private constant [%s x i8] c\"%s\"\n",
                cad.getNombreIR(), cad.getLongitudIR(), cad.getValorIR())));
    }


    /*** Función principal ***/

    public String generarCodigo(Programa p, String nombreArchivoFuente, Boolean comentariosOn) throws ExcepcionVisitor {
        this.nombreArchivoFuente = nombreArchivoFuente;
        this.tablaFunciones = p.getTablaFunciones();
        this.arrCadenas = p.getArrCadenas();
        this.comentariosOn = comentariosOn;

        super.procesar(p);
        return codigo.toString();
    }

    /**** Visitors ***/

    /* Base */
    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
        codigo = new StringBuilder();
        codigo.append(String.format("; Programa: %s\n", p.getNombre()))
              .append(String.format("source_filename = \"%s\"\n", nombreArchivoFuente));

        try {
            codigo.append(getHostTarget());
        } catch (IOException | InterruptedException e) {
            // algo falló... dejarlo hardcodeado
            if (targetEsWindows()) {
                // Target Bruno
                codigo.append("target datalayout = \"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n")
                      .append("target triple = \"x86_64-pc-windows-msvc19.28.29335\"\n");
            } else {
                // Target Franco
                codigo.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n")
                      .append("target triple = \"x86_64-pc-linux-gnu\"\n");
            }
        }

        codigo.append("\n\n");

        // Definir globalmente sólo lo necesario

        Set<String> funPredefUsadas = p.getFunPredefUsadas();
        final boolean usaWriteln = funPredefUsadas.contains("writeln");
        final boolean usaWrite = funPredefUsadas.contains("write") || usaWriteln;
        final boolean usaReadBoolean = funPredefUsadas.contains("read_boolean");
        final boolean usaReadInteger = funPredefUsadas.contains("read_integer");
        final boolean usaReadFloat = funPredefUsadas.contains("read_float");
        final boolean usaRead = usaReadBoolean || usaReadInteger || usaReadFloat;

        if (usaWrite) {
            codigo.append("declare i32 @printf(i8*, ...)\n");
        }
        if (usaRead) {
            codigo.append("declare i32 @scanf(i8*, ...)\n");
        }
        if (usaWrite || usaRead) {
            codigo.append("\n; Constantes para entradas y salidas\n")
                  .append("@.int_format = private constant [3 x i8] c\"%d\\00\"\n")
                  .append("@.double_format = private constant [4 x i8] c\"%lf\\00\"\n")
                  // Imprimir floats siempre con dos decimales
                  .append("@.double_print_format = private constant [6 x i8] c\"%.2lf\\00\"\n")
                  .append("@.bln_true_format = private constant [5 x i8] c\"true\\00\"\n")
                  .append("@.bln_false_format = private constant [6 x i8] c\"false\\00\"\n");
        }
        if (usaWriteln) {
            codigo.append("@.salto_linea = private constant [2 x i8] c\"\\0A\\00\"\n");
        }
        codigo.append("\n");

        if (usaReadBoolean) imprimirDefinicionLeer(Tipo.BOOLEAN);
        if (usaReadInteger) imprimirDefinicionLeer(Tipo.INTEGER);
        if (usaReadFloat) imprimirDefinicionLeer(Tipo.FLOAT);

        declararVarsStrs();

        super.visit(p);
    }

    @Override
    public void visit(Bloque b) throws ExcepcionVisitor {
        if (b.esProgramaPrincipal()) {
            codigo.append("\ndefine i32 @main(i32, i8**) {\n");
            inicializarVarsGbls();
            super.visit(b);
            imprimirCodigo("ret i32 0");
            codigo.append("}\n");
        } else {
            super.visit(b);
        }
    }


    /* Sentencia de asignación */

    @Override
    public void visit(Asignacion asig) throws ExcepcionVisitor {
        Expresion expr = asig.getExpresion();
        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();

        boolean aplicarCortocircuito = (expr instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) {
            grarEtiCortocircuitoAsig("asig");
        }

        // Anexar declaración de referencias necesarias para la parte derecha de la asig.
        expr.accept(this);

        // Origen va a contener la referencia al valor de la expresión
        String origen = expr.getRefIR();
        String destino = svDestino.getNombreIR();

        imprimirComent(String.format("visit(Asignacion)%s: %s = %s",
                aplicarCortocircuito ? " con cortocircuito booleano" : "",
                svDestino.getNombre(), expr));

        if (aplicarCortocircuito) {
            finalizarCortocircuitoAsig(origen, destino);
        } else {
            String tipoOrigen = expr.getTipo().getIR();
            String tipoDestino = svDestino.getTipo().getIR();
            // TipoOrigen y tipoDestino deberían ser iguales, pero lo dejo así para detectar algún error
            // y de paso usar los nombres de las variables para que quede un poco más claro lo que se hace

            imprimirCodigo(String.format("store %s %s, %s* %s", tipoOrigen, origen, tipoDestino, destino));
        }
    }


    /* Sentencias de declaración */

    @Override
    public void visit(DecVar dv) {
        // Genera la declaración de una variable que no fue inicializada

        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = sv.getTipo().getIR();
        Boolean esGlobal = sv.getEsGlobal();
        String valorIR = dv.getTipo().getValorDefIR();

        imprimirComent(String.format("visit(DecVar): variable %s is %s = %s", sv.getNombre(), sv.getTipo(), valorIR));

        if (esGlobal) {
            codigo.append(String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            imprimirCodigo(String.format("%s = alloca %s", nombreIR, tipoIR));
            imprimirCodigo(String.format("store %2$s %3$s, %2$s* %1$s", nombreIR, tipoIR, valorIR));
        }
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        // Genera la declaración de una variable que sí fue inicializada

        Expresion expr = dvi.getExpresion();
        SimboloVariable sv = (SimboloVariable) dvi.getIdent();

        boolean aplicarCortocircuito = (expr instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) {
            grarEtiCortocircuitoAsig("decvarini");
        }

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = sv.getTipo().getIR();

        if (sv.getEsGlobal()) {
            imprimirComent(String.format("\n;visit(DecVarIni)%s: variable %s is %s = %s",
                    aplicarCortocircuito ? " con cortocircuito booleano" : "",
                    sv.getNombre(), sv.getTipo(), expr));

            // Le asigno temporalmente a la var. el valor por defecto según
            // su tipo, porque no puedo inicializarla en el alcance global.
            String valorDef = sv.getTipo().getValorDefIR();
            codigo.append(String.format("\n%s = global %s %s\n", nombreIR, tipoIR, valorDef));

            // Creo una función que se va a llamar en el main para inicializar la var. con el valor correspondiente
            String nomFunAux = Normalizador.crearNomFun("init.var.gbl");
            codigo.append(String.format("define void %s() {\n", nomFunAux));
            expr.accept(this);
            String refIR = expr.getRefIR();

            if (aplicarCortocircuito) {
                finalizarCortocircuitoAsig(refIR, sv.getNombreIR());
            } else {
                imprimirCodigo(String.format("store %1$s %2$s, %1s* %3$s", tipoIR, refIR, nombreIR));
                imprimirCodigo("ret void");
                codigo.append("}\n");

                // Guardo el nombre de la función para invocarla en el main
                varGblInit.add(nomFunAux);
            }
        } else {
            // Variable local

            // Visito a la expresión para generar la declaración de referencias necesarias
            expr.accept(this);

            // El refIR con el valor de la expresión viene resuelto gracias a la visita anterior
            String refIR;
            refIR = expr.getRefIR();

            imprimirComent(String.format("visit(DecVarIni)%s: variable %s is %s = %s",
                    aplicarCortocircuito ? " con cortocircuito booleano" : "",
                    sv.getNombre(), sv.getTipo(), refIR));

            if (aplicarCortocircuito) {
                finalizarCortocircuitoAsig(refIR, sv.getNombreIR());
            } else {
                imprimirCodigo(String.format("%s = alloca %s", nombreIR, tipoIR));
                imprimirCodigo(String.format("store %2$s %3$s, %2$s* %1$s", nombreIR, tipoIR, refIR));
            }
        }
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo de retorno, nombre, parámetros y el cuerpo
        String funTipoRetIR = simboloFun.getTipo().getIR();
        String funNombreIR = simboloFun.getNombreIR();

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        String params = grarStrParams(df.getParams());
        codigo.append(String.format("\ndefine %s %s(%s) {\n", funTipoRetIR, funNombreIR, params));

        // Anexar referencias de los parámetros al principio de la función
        for (Param param : df.getParams()) {
            param.accept(this);
        }

        // Anexar cuerpo de la función y cerrar
        df.getBloque().accept(this);

        if (!(df.getTieneRetorno())) {
            // Para evitar comportamientos indefinidos, si la función no tiene retorno,
            // le genero uno en base al valor por defecto asociado a su tipo.
            String valorDef = df.getTipo().getValorDefIR();
            imprimirCodigo(String.format("ret %s %s", funTipoRetIR, valorDef));
        }

        codigo.append("}\n");
    }

    @Override
    public void visit(Param p) {
        /* Para poder utilizar el parámetro creo una variable auxiliar,
         * para la cual genero un nombreIR y un refIR, que pisan al que
         * viene en el objeto SimboloVariable del parámetro. Después
         * guardo el valor que viene en el parámetro en esta "nueva" var.
         * Esto supone que el pasaje es por valor y no por referencia.
         * Si el parámetro fue inicializado por defecto o no es indistinto.
         */

        SimboloVariable sv = (SimboloVariable) p.getIdent();

        // Guardo el nombre original del parámetro para poder extraer el valor, después lo piso
        String nombreOriginal = sv.getNombreIR();
        String refIR = Normalizador.crearNomPtroLcl("ref");
        String nombreIR = Normalizador.crearNomPtroLcl("param");
        String tipoIR = p.getTipo().getIR();

        sv.setRefIR(refIR);
        sv.setNombreIR(nombreIR);

        imprimirComent(String.format("visit(Param): %s", sv.getNombre()));

        imprimirCodigo(String.format("%s = alloca %s", nombreIR, tipoIR));
        imprimirCodigo(String.format("store %2$s %3$s, %2$s* %1$s", nombreIR, tipoIR, nombreOriginal),
                       String.format("%1$s = %2$s", nombreIR, nombreOriginal));
    }

    @Override
    public void visit(ParamDef pi) throws ExcepcionVisitor {
        // El valor del parámetro ya debería venir resuelto desde la invocación,
        // por lo que puedo llamar a visit(Param) directamente.
        visit((Param) pi);
    }


    /* Sentencias de selección */

    @Override
    public void visit(SiEntonces se) throws ExcepcionVisitor {
        String etiBlqThen = Normalizador.crearNomEtiqueta("blq_then");
        String etiFin = Normalizador.crearNomEtiqueta("fin_if");
        etiquetasOpBinLog.push(new Pair<>(etiBlqThen, etiFin));

        imprimirComent("visit(SiEntonces)");

        // Salto condicional
        se.getCondicion().accept(this);
        String refCond = se.getCondicion().getRefIR();
        imprimirCodSaltoCond(refCond, etiBlqThen, etiFin);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        se.getBloqueSiEntonces().accept(this);
        imprimirCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);

        etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        String etiBlqThen = Normalizador.crearNomEtiqueta("blq_then");
        String etiBlqElse = Normalizador.crearNomEtiqueta("blq_else");
        String etiFin = Normalizador.crearNomEtiqueta("fin_if");

        imprimirComent("visit(SiEntoncesSino)");

        // Salto condicional
        ses.getCondicion().accept(this);
        String refCond = ses.getCondicion().getRefIR();
        imprimirCodSaltoCond(refCond, etiBlqThen, etiBlqElse);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        ses.getBloqueSiEntonces().accept(this);
        imprimirCodSaltoInc(etiFin);

        // Caso false
        imprimirEtiqueta(etiBlqElse);
        ses.getBloqueSino().accept(this);
        imprimirCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);
    }


    /* Sentencias de iteración */

    @Override
    public void visit(Mientras m) throws ExcepcionVisitor {
        String etiInicioWhile = Normalizador.crearNomEtiqueta("inicio_while");
        String etiBucleWhile = Normalizador.crearNomEtiqueta("bucle_while");
        String etiFinWhile = Normalizador.crearNomEtiqueta("fin_while");
        Pair<String, String> parEtiquetas = new Pair<>(etiBucleWhile, etiFinWhile);
        etiquetasMientras.push(parEtiquetas);

        imprimirComent("visit(While)");

        imprimirCodSaltoInc(etiInicioWhile);
        imprimirEtiqueta(etiInicioWhile);

        Expresion condicion = m.getCondicion();
        boolean aplicarCortocircuito = (condicion instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) etiquetasOpBinLog.push(parEtiquetas);

        // Generar ref. al resultado de la condición
        condicion.accept(this);

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        if (condicion instanceof NegacionLogica) {
            // Invertir etiquetas de salto
            imprimirCodSaltoCond(condicion.getRefIR(), etiFinWhile, etiBucleWhile);
        } else {
            imprimirCodSaltoCond(condicion.getRefIR(), etiBucleWhile, etiFinWhile);
        }
        imprimirEtiqueta(etiBucleWhile);

        // Generar cuerpo del while
        m.getBloqueSentencias().accept(this);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        imprimirCodSaltoInc(etiInicioWhile);

        imprimirEtiqueta(etiFinWhile);

        etiquetasMientras.pop();
        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }


    /* Sentencias de control */

    @Override
    public void visit(Retorno r) throws ExcepcionVisitor {
        Expresion expr = r.getExpresion();

        boolean aplicarCortocircuito = (expr instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) grarEtiCortocircuitoAsig("decvarini");

        // Generar refIR para la expresión de retorno
        expr.accept(this);

        String tipoRetorno = expr.getTipo().getIR();
        String refRetorno = expr.getRefIR();

        imprimirCodigo(String.format("ret %s %s", tipoRetorno, refRetorno));

        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(Continuar c) {
        imprimirComent("visit(Continuar)");
        Pair<String, String> etiquetas = etiquetasMientras.peek();
        // Saltar al principio del while
        imprimirCodSaltoInc(etiquetas.fst);
    }

    @Override
    public void visit(Salir s) {
        imprimirComent("visit(Salir)");
        Pair<String, String> etiquetas = etiquetasMientras.peek();
        // Saltar al final del while
        imprimirCodSaltoInc(etiquetas.snd);
    }


    /* Operaciones */

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionVisitor {
        if (ob instanceof OperacionBinariaLogica) {
            // Manejar operaciones lógicas usando cortocircuito booleano.
            imprimirCortocircuito((OperacionBinariaLogica) ob);
            return;
        }

        // Defino un nombre auxiliar con el cual puedo referenciar el valor de la expr.
        String refIR = Normalizador.crearNomRef("ob");
        ob.setRefIR(refIR);

        imprimirComent(String.format("visit(OperacionBinaria): %s %s %s",
                ob.getIzquierda().toString(), ob.getNombre(), ob.getDerecha().toString()));

        // El padre visita a las exprs. izq. y der. para generar la declaración de referencias
        super.visit(ob);

        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instIR = ob.getInstruccionIR();
        String tipoIR = ob.getIzquierda().getTipo().getIR();
        String operadorParser = ob.getNombre();

        if (ob instanceof OperacionBinariaAritmetica) {
            // Por ej.: %aux.ob.11 = add i32 %aux.sv.9, %aux.ref.10 ; %aux.ob.11 = %aux.sv.9 + %aux.ref.10
            imprimirCodigo(String.format("%s = %s %s %s, %s", refIR, instIR, tipoIR, refIzqIR, refDerIR),
                           String.format("%s = %s %s %s", refIR, refIzqIR, operadorParser, refDerIR));
        } else if (ob instanceof Relacion) {
            String tipoCmp = ((Relacion) ob).getTipoCmp();
            // Por ej.: %aux.ob.15 = icmp sgt i32 %aux.sv.13, %aux.sv.14 ; %aux.sv.13 > %aux.sv.14
            imprimirCodigo(String.format("%s = %s %s %s %s, %s",
                                         refIR, tipoCmp, instIR, tipoIR, refIzqIR, refDerIR),
                           String.format("%s %s %s", refIzqIR, operadorParser, refDerIR));
        } else {
            throw new ExcepcionVisitor("Tipo de operación binaria inesperado.");
        }
    }


    /* Valores */

    @Override
    public void visit(Literal lit) throws ExcepcionVisitor {
        /* Este visitor genera una variable auxiliar para utilizar los valores literales.
         * Como alternativa a generar la variable podríamos utilizar directamente el valor,
         * pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.
         */

        String refIR = Normalizador.crearNomRef("lit");
        lit.setRefIR(refIR);

        Tipo tipoParser = lit.getTipo();
        String tipoIR = tipoParser.getIR();
        String valorParser = lit.getValor();
        String valorIR;

        if (tipoParser == Tipo.INTEGER) {
            valorIR = valorParser;
        } else if (tipoParser == Tipo.FLOAT) {
            // ??? De verdad que no se puede hacer más simple esto, cuando imprimo tengo que volver a convertirlo
            double temp = Float.parseFloat(valorParser);
            valorIR = Double.toString(temp);
        } else if (tipoParser == Tipo.BOOLEAN) {
            valorIR = valorParser.equals("false") ? "0" : "1";
        } else {
            throw new ExcepcionVisitor("Valor de tipo inesperado: " + lit.getTipo());
        }

        imprimirComent(String.format("visit(Literal): %s", valorParser));

        String valorNeutro = lit.getTipo() == Tipo.FLOAT ? "0.0" : "0";
        String instSuma = lit.getTipo() == Tipo.FLOAT ? "fadd" : "add";
        // Hack para generar referencias a valores en una línea (le sumo 0 al valor que quiero guardar)
        imprimirCodigo(String.format("%s = %s %s %s, %s", refIR, instSuma, tipoIR, valorIR, valorNeutro));
    }

    @Override
    public void visit(Identificador ident) {
        // Genera el store sobre un refIR para poder acceder a una variable

        // En esta etapa este Identificador va a ser siempre un SimboloVariable. Tengo que utilizarlo así porque
        // si creo visit(SimboloVariable) da muchos problemas (por ej. se rompe el graficado del AST)
        SimboloVariable sv = (SimboloVariable) ident;

        String nombreIR = sv.getNombreIR();
        String tipoIR = sv.getTipo().getIR();

        String nombreVar = sv.getTipo().equals(Tipo.INTEGER) ? "int" :
                sv.getTipo().equals(Tipo.FLOAT) ? "dbl" :
                sv.getTipo().equals(Tipo.BOOLEAN) ? "bln" : "unknown";

        String refIR = Normalizador.crearNomRef(nombreVar);
        sv.setRefIR(refIR);

        imprimirComent(String.format("visit(Identificador): %s", sv.getNombre()));

        imprimirCodigo(String.format("%1$s = load %2$s, %2$s* %3$s", refIR, tipoIR, nombreIR));
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionVisitor {
        imprimirComent(String.format("visitInvocacionFuncion: %s()", i.getNombre()));

        // El refIR que va a contener el valor de la invocación a la función
        String refIR = Normalizador.crearNomRef("invo");
        i.setRefIR(refIR);

        // A las invocaciones a write las manejamos aparte
        if (i.getNombre().equals("write") || i.getNombre().equals("writeln")) {
            imprimirWrite(i);
            return;
        }
        // A las funciones read las definimos como funciones en IR, y las invocamos normalmente

        // Buscar la función en la tabla
        SimboloFuncion sf = tablaFunciones.get(i.getNombre());

        String nombreFun = sf.getNombreIR();
        String tipoFun = sf.getTipo().getIR();

        // Generar la lista de argumentos, además de visitarlos para generar las refs.
        String args = grarStrArgs(i.getArgs());

        imprimirCodigo(String.format("%s = call %s %s(%s)", refIR, tipoFun, nombreFun, args));
    }
}
