package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.util.GenerarIR;
import ar.edu.unnoba.compilador.util.Normalizador;
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
import ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas.NegacionAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.OperacionConversion;
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
    private String nombreArchivoFuente;

    private GenerarIR grar;

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

    /*** Funciones auxiliares ***/

    public static boolean targetEsWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

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

    /* Para tratar los casos de invocaciones a write */
    private void imprimirWrite(InvocacionFuncion i) throws ExcepcionVisitor {
        Expresion arg = i.getArgs().get(0);
        arg.accept(this);
        String refIR = arg.getRefIR();
        grar.setComentLinea(i.getEtiqueta());

        if (arg instanceof Cadena) {
            Cadena cad = (Cadena) arg;
            grar.print(cad.getNombreIR(), cad.getLongitudIR());
        } else if (arg.getTipo().equals(Tipo.BOOLEAN)) {
            // Extender el i1 a i32 para poder compararlo
            String refExt = Normalizador.crearNomRef("ext");
            grar.zext(refExt, "i1", refIR);
            refIR = refExt;

            // Ahora se evalúa el valor del booleano para imprimir "true" o "false".
            String casoTrue = Normalizador.crearNomEtiqueta("bln_true");
            String casoFalse = Normalizador.crearNomEtiqueta("bln_false");
            String fin = Normalizador.crearNomEtiqueta("fin_eval_bln");

            String refAux = Normalizador.crearNomRef("aux");

            // Guardo en refAux el valor de la comparación
            grar.saltoIgual(refAux, "i32", refIR, "1", casoTrue, casoFalse);

            grar.etiqueta(casoTrue);
            grar.print("@.bln_true_format", 5);
            grar.salto(fin);

            grar.etiqueta(casoFalse);
            grar.print("@.bln_false_format", 6);
            grar.salto(fin);

            grar.etiqueta(fin);
        } else {
            // Es un número entero o flotante
            String tipoIR = arg.getTipo().equals(Tipo.INTEGER) ? "i32" : "double";

            if (arg.getTipo().equals(Tipo.INTEGER)) {
                grar.print("@.int_format", 3, tipoIR, refIR);
            } else {
                grar.print("@.double_print_format", 6, tipoIR, refIR);
            }
        }

        if (i.getNombre().equals("writeln")) {
            // Genero un salto de línea
            grar.print("@.salto_linea", 2);
        }
    }

    // Generar implementación de read_integer o read_float
    private void imprimirDefLeer(Tipo tipo) throws ExcepcionVisitor {
        // No hace falta generarles un id a los registros y etiquetas;
        // son únicas dentro de la función IR
        final String ptroValor = "%ptro.valor_leido";
        final String refValor = "%ref.valor_leido";
        final String tipoIR = tipo.getIR();

        grar.defFuncion("@read_" + tipo, tipoIR, "");
        grar.alloca(ptroValor, tipoIR);

        if (tipo.equals(Tipo.FLOAT)) {
            grar.scan("@.double_format", 4, tipoIR, ptroValor);
        } else{
            grar.scan("@.int_format", 3, tipoIR, ptroValor);
        }

        grar.load(refValor, tipoIR, ptroValor);
        grar.ret(tipoIR, refValor);
        grar.cierreBloque();
    }

    /* En read_boolean(), leemos caracteres. Si el primero es 't' o 'T' asumimos que es true.
     * Era más fácil con 0 y 1... */
    private void imprimirDefLeerBoolean() {
        final String tipoRet = "i1";
        final String tipoLeido = "i8";
        final String ptroValor = "%ptro.valor_leido";
        String refValor = "%ref.valor_leido.1";
        String refExt = "%ref.ext";

        grar.defFuncion("@read_boolean", tipoRet, "");

        // Generar la variable a retornar
        final String ptroRet = "%ptro.ret";
        final String refRet = "%ref.ret";
        grar.alloca(ptroRet, tipoRet);

        grar.alloca(ptroValor, tipoLeido);
        grar.scan("@.char_format", 3, tipoLeido, ptroValor);
        grar.load(refValor, tipoLeido, ptroValor);

        // Extender a i32 para comparar el carácter leído
        grar.sext(refExt, tipoLeido, refValor);

        grar.setComentLinea("comparar con 't'");
        grar.saltoIgual("%ref.cmp.1", "i32", refExt, "116", "verdadero", "no_es_t");

        grar.etiqueta("no_es_t");
        grar.setComentLinea("comparar con 'T'");
        grar.saltoIgual("%ref.cmp.2", "i32", refExt, "84", "verdadero", "falso");

        grar.etiqueta("verdadero");
        grar.store(ptroRet, tipoRet, "true");
        grar.salto("leer_chars");

        grar.etiqueta("falso");
        grar.store(ptroRet, tipoRet, "false");
        grar.salto("leer_chars");

        refValor = "%ref.valor_leido.2";
        refExt = "%ref.ext.2";

        grar.etiqueta("leer_chars");
        grar.coment("Seguir leyendo caracteres hasta que se ingrese retorno,");
        grar.coment("para que no los tome como entrada después");
        grar.scan("@.char_format", 3, tipoLeido, ptroValor);
        grar.load(refValor, tipoLeido, ptroValor);
        grar.sext(refExt, tipoLeido, refValor);
        grar.setComentLinea("comparar con '\\n'");
        grar.saltoIgual("%ref.cmp.3", "i32", refExt, "10", "ret", "leer_chars");

        grar.etiqueta("ret");
        grar.load(refRet, tipoRet, ptroRet);
        grar.ret(tipoRet, refRet);
        grar.cierreBloque();
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

    private String grarStrArgs(List<Expresion> arrArgs, List<Param> paramsFormales) throws ExcepcionVisitor {
        // Similar a grarStrParams, pero esta lista es utilizada por las invocaciones, o sea el
        // argumento puede ser una variable, un literal o una expresión más compleja, mientras
        // que en la declaración de la función eso va a ser siempre un objeto de tipo Param.

        StringBuilder strArgs = new StringBuilder();

        for (int i = 0; i < paramsFormales.size(); i++) {
            // Si el argumento no existe, tomo el parámetro formal para crear una ref. con el valor definido
            Expresion expr = i >= arrArgs.size() ?
                    ((ParamDef) paramsFormales.get(i)).getExpresion() :
                    arrArgs.get(i);

            // Evaluar la expr. y generar el refIR
            expr.accept(this);

            // Para separar los argumentos mediante comas, excepto el final
            String sep = i != paramsFormales.size() - 1 ? ", " : "";

            // Añado el argumento a la lista
            strArgs.append(String.format("%s %s%s", expr.getTipo().getIR(), expr.getRefIR(), sep));
        }

        return strArgs.toString();
    }

    /*** Funciones auxiliares para generar el cortocircuito booleano ***/

    private void imprimirCortocircuito(OperacionBinariaLogica ob) throws ExcepcionVisitor {
        Pair<String, String> etiquetas = etiquetasOpBinLog.peek();
        String etiVerdadero = etiquetas.fst;
        String etiFalso = etiquetas.snd;

        grar.coment(String.format("Cortocircuito booleano: %s", ob));

        Expresion expIzquierda = ob.getIzquierda();
        if (expIzquierda instanceof NegacionLogica) ((NegacionLogica) expIzquierda).setEnCortocircuito(true);

        expIzquierda.accept(this);

        final String refIR = expIzquierda.getRefIR();
        final String etiTmp;

        if (ob instanceof Conjuncion) { // AND
            // Si el operador izquierdo es falso, esta operación es falsa
            etiTmp = Normalizador.crearNomEtiqueta("and_verdadero");
            if (expIzquierda instanceof NegacionLogica) {
                grar.salto(refIR, etiFalso, etiTmp);
            } else {
                grar.salto(refIR, etiTmp, etiFalso);
            }
        } else if (ob instanceof Disyuncion) { // OR
            // Si el operador izquierdo es verdadero, esta operación es verdadera
            etiTmp = Normalizador.crearNomEtiqueta("or_falso");
            if (expIzquierda instanceof NegacionLogica) {
                grar.salto(refIR, etiTmp, etiVerdadero);
            } else {
                grar.salto(refIR, etiVerdadero, etiTmp);
            }
        } else {
            throw new ExcepcionVisitor("Tipo de operación binaria lógica inesperado.");
        }

        grar.etiqueta(etiTmp);

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
        grar.salto(refIR, parEtiquetas.fst, parEtiquetas.snd);

        // Resultado verdadero
        grar.etiqueta(parEtiquetas.fst);
        grar.store(nombreIR, "i1", "1");
        grar.salto(etiFin);

        // Resultado falso
        grar.etiqueta(parEtiquetas.snd);
        grar.store(nombreIR, "i1", "0");
        grar.salto(etiFin);

        grar.etiqueta(etiFin);
    }

    /* Invoca a las funciones que asignan a las variables
     * globales el valor con el que fueron declaradas */
    private void inicializarVarsGbls() {
        varGblInit.forEach(fun -> grar.invocacion(fun, ""));
    }

    /* Inicializa las cadenas que van a usarse en el programa
     */
    private void declararVarsStrs() {
        // Por ej.: @ptro.str.2 = private constant [5 x i8] c"Hola\00"
        arrCadenas.forEach(cad ->
                grar.cadena(cad.getNombreIR(), cad.getValorIR(), cad.getLongitudIR()));
    }


    /*** Función principal ***/

    public String generarCodigo(Programa p, String nombreArchivoFuente) throws ExcepcionVisitor {
        this.nombreArchivoFuente = nombreArchivoFuente;
        this.tablaFunciones = p.getTablaFunciones();
        this.arrCadenas = p.getArrCadenas();

        grar = new GenerarIR();
        super.procesar(p);
        return grar.getCodigo();
    }

    /**** Visitors ***/

    /* Base */
    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
        StringBuilder sb = new StringBuilder();

        String target;
        try {
            target = getHostTarget();
        } catch (IOException | InterruptedException e) {
            // algo falló... dejarlo fijo
            if (targetEsWindows()) {
                // Target Bruno
                target = "target datalayout = \"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n" +
                        "target triple = \"x86_64-pc-windows-msvc19.28.29335\"\n";
            } else {
                // Target Franco
                target = "target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n" +
                        "target triple = \"x86_64-pc-linux-gnu\"\n";
            }
        }

        // Generar encabezado

        sb.append(String.format("; Programa: %s\n", p.getNombre()))
                .append(String.format("source_filename = \"%s\"\n", nombreArchivoFuente))
                .append(target);

        // Definir globalmente sólo lo necesario

        Set<String> funPredefUsadas = p.getFunPredefUsadas();
        final boolean usaWriteln = funPredefUsadas.contains("writeln");
        final boolean usaWrite = funPredefUsadas.contains("write") || usaWriteln;
        final boolean usaReadBoolean = funPredefUsadas.contains("read_boolean");
        final boolean usaReadInteger = funPredefUsadas.contains("read_integer");
        final boolean usaReadFloat = funPredefUsadas.contains("read_float");
        final boolean usaRead = usaReadBoolean || usaReadInteger || usaReadFloat;

        if (usaWrite) {
            sb.append("declare i32 @printf(i8*, ...)\n");
        }
        if (usaRead) {
            sb.append("declare i32 @scanf(i8*, ...)\n");
        }
        if (usaWrite || usaRead) {
            sb.append("\n; Constantes para entradas y salidas\n")
                    .append("@.int_format = private constant [3 x i8] c\"%d\\00\"\n")
                    .append("@.double_format = private constant [4 x i8] c\"%lf\\00\"\n");
        }
        if (usaRead) {
            // Para leer char y convertirlo a boolean.
            sb.append("@.char_format = private constant [3 x i8] c\"%c\\00\"\n");
        }
        if (usaWrite) {
            // Imprimir double siempre con dos decimales
            sb.append("@.double_print_format = private constant [6 x i8] c\"%.2lf\\00\"\n")
                    .append("@.bln_true_format = private constant [5 x i8] c\"true\\00\"\n")
                    .append("@.bln_false_format = private constant [6 x i8] c\"false\\00\"\n");
        }
        if (usaWriteln) {
            sb.append("@.salto_linea = private constant [2 x i8] c\"\\0A\\00\"");
        }
        sb.append("\n");

        grar.codigo(sb.toString(), false);

        if (usaReadBoolean) imprimirDefLeerBoolean();
        if (usaReadInteger) imprimirDefLeer(Tipo.INTEGER);
        if (usaReadFloat) imprimirDefLeer(Tipo.FLOAT);

        declararVarsStrs();

        super.visit(p);
    }

    @Override
    public void visit(Bloque b) throws ExcepcionVisitor {
        if (b.esProgramaPrincipal()) {
            grar.defFuncion("@main", "i32", "i32, i8**");
            inicializarVarsGbls();
            super.visit(b);
            grar.ret("i32", "0");
            grar.cierreBloque();
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

        grar.setComentLinea(String.format("%s %s %s%s",
                svDestino.getNombre(), asig.getEtiqueta(), expr,
                aplicarCortocircuito ? " - cortocircuito" : ""));
        if (aplicarCortocircuito) {
            finalizarCortocircuitoAsig(origen, destino);
        } else {
            grar.store(destino, svDestino.getTipo().getIR(), origen);
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

        grar.setComentLinea(String.format("variable %s is %s = %s", sv.getNombre(), sv.getTipo(), valorIR));

        if (esGlobal) {
            grar.global(nombreIR, tipoIR, valorIR);
        } else {
            grar.alloca(nombreIR, tipoIR);
            grar.store(nombreIR, tipoIR, valorIR);
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

        grar.setComentLinea(String.format("variable %s is %s = %s%s",
                sv.getNombre(), sv.getTipo(), expr,
                aplicarCortocircuito ? " - cortocircuito" : ""));

        if (sv.getEsGlobal()) {
            // Le asigno temporalmente a la var. el valor por defecto según
            // su tipo, porque no puedo inicializarla en el alcance global.
            String valorDef = sv.getTipo().getValorDefIR();
            grar.global(nombreIR, tipoIR, valorDef);

            // Creo una función que se va a llamar en el main para inicializar la var. con el valor correspondiente
            String nomFunAux = Normalizador.crearNomFun("init.var.gbl");
            grar.defFuncion(nomFunAux, "void", "");
            expr.accept(this);
            String refIR = expr.getRefIR();

            if (aplicarCortocircuito) {
                finalizarCortocircuitoAsig(refIR, sv.getNombreIR());
            } else {
                grar.store(nombreIR, tipoIR, refIR);
                grar.ret();
                grar.cierreBloque();

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

            if (aplicarCortocircuito) {
                finalizarCortocircuitoAsig(refIR, sv.getNombreIR());
            } else {
                grar.alloca(nombreIR, tipoIR);
                grar.store(nombreIR, tipoIR, refIR);
            }
        }
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo de retorno, nombre,
        // la etiqueta de retorno, parámetros y el cuerpo
        String tipoRet = simboloFun.getTipo().getIR();
        String nombreFun = simboloFun.getNombreFuncionIR();
        String ptroRet = simboloFun.getPtroRet();
        String refRet = simboloFun.getRefIR();
        String etiquetaFin = df.getEtiquetaFin();
        String valorPorDef = df.getTipo().getValorDefIR();

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        String params = grarStrParams(df.getParams());
        grar.defFuncion(nombreFun, tipoRet, params);

        // Inicializo el retorno de la función con el valor por defecto
        grar.alloca(ptroRet, tipoRet);
        grar.store(ptroRet, tipoRet, valorPorDef);

        // Generar referencias a los parámetros
        for (Param param : df.getParams()) {
            param.accept(this);
        }

        // Generar cuerpo de la función
        df.getBloque().accept(this);

        /* Tengo un único retorno para toda la función. Si no se pisó el valor de retorno
         * en algun visit(Retorno), se devuelve el valor por defecto que fue asignado al
         * inicio de la declaración para evitar comportamientos indefinidos.
         */

        grar.salto(etiquetaFin);
        grar.etiqueta(etiquetaFin);

        // Cargo el valor de retorno de la función
        grar.load(refRet, tipoRet, ptroRet);
        grar.ret(tipoRet, refRet);
        grar.cierreBloque();
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

        grar.alloca(nombreIR, tipoIR);
        grar.setComentLinea(String.format("Param %s", sv.getNombre()));
        grar.store(nombreIR, tipoIR, nombreOriginal);
    }

    @Override
    public void visit(ParamDef pi) {
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

        grar.coment("if / then");

        // Salto condicional
        se.getCondicion().accept(this);
        String refCond = se.getCondicion().getRefIR();
        grar.salto(refCond, etiBlqThen, etiFin);

        // Caso true
        grar.etiqueta(etiBlqThen);
        se.getBloqueSiEntonces().accept(this);
        grar.salto(etiFin);

        // Fin if
        grar.etiqueta(etiFin);

        etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        String etiBlqThen = Normalizador.crearNomEtiqueta("blq_then");
        String etiBlqElse = Normalizador.crearNomEtiqueta("blq_else");
        String etiFin = Normalizador.crearNomEtiqueta("fin_if");

        grar.coment("if / then / else");

        // Salto condicional
        ses.getCondicion().accept(this);
        String refCond = ses.getCondicion().getRefIR();
        grar.salto(refCond, etiBlqThen, etiBlqElse);

        // Caso true
        grar.etiqueta(etiBlqThen);
        ses.getBloqueSiEntonces().accept(this);
        grar.salto(etiFin);

        // Caso false
        grar.etiqueta(etiBlqElse);
        ses.getBloqueSino().accept(this);
        grar.salto(etiFin);

        // Fin if
        grar.etiqueta(etiFin);
    }


    /* Sentencias de iteración */

    @Override
    public void visit(Mientras m) throws ExcepcionVisitor {
        String etiInicioWhile = Normalizador.crearNomEtiqueta("inicio_while");
        String etiBucleWhile = Normalizador.crearNomEtiqueta("bucle_while");
        String etiFinWhile = Normalizador.crearNomEtiqueta("fin_while");
        Pair<String, String> parEtiquetas = new Pair<>(etiBucleWhile, etiFinWhile);
        etiquetasMientras.push(parEtiquetas);

        grar.coment("while");

        grar.salto(etiInicioWhile);
        grar.etiqueta(etiInicioWhile);

        Expresion condicion = m.getCondicion();
        boolean aplicarCortocircuito = (condicion instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) etiquetasOpBinLog.push(parEtiquetas);
        if (condicion instanceof NegacionLogica) ((NegacionLogica) condicion).setEnCortocircuito(true);

        // Generar ref. al resultado de la condición
        condicion.accept(this);

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        if (condicion instanceof NegacionLogica) {
            // Invertir etiquetas de salto
            grar.salto(condicion.getRefIR(), etiFinWhile, etiBucleWhile);
        } else {
            grar.salto(condicion.getRefIR(), etiBucleWhile, etiFinWhile);
        }
        grar.etiqueta(etiBucleWhile);

        // Generar cuerpo del while
        m.getBloqueSentencias().accept(this);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        grar.salto(etiInicioWhile);

        grar.etiqueta(etiFinWhile);

        etiquetasMientras.pop();
        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }


    /* Sentencias de control */

    @Override
    public void visit(Retorno r) throws ExcepcionVisitor {
        SimboloFuncion simboloFun = tablaFunciones.get(r.getFun().getNombre());
        Expresion expr = r.getExpresion();

        boolean aplicarCortocircuito = (expr instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) grarEtiCortocircuitoAsig("decvarini");

        // Generar refIR para la expresión de retorno
        expr.accept(this);

        String tipoIR = simboloFun.getTipo().getIR();
        // La variable con el valor de la expresión resuelto
        String refExpr = expr.getRefIR();
        // El puntero del cual se toma el valor al hacer el return al final de la fun.
        String ptroRet = simboloFun.getPtroRet();

        // Guardo el valor de la expr. del return en la variable que se va a retornar al final de la función
        grar.store(ptroRet, tipoIR, refExpr);

        // Salto al final de la función
        grar.salto(r.getFun().getEtiquetaFin());

        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(Continuar c) {
        Pair<String, String> etiquetas = etiquetasMientras.peek();
        grar.setComentLinea("continue");
        // Saltar al principio del while
        grar.salto(etiquetas.fst);
    }

    @Override
    public void visit(Salir s) {
        Pair<String, String> etiquetas = etiquetasMientras.peek();
        grar.setComentLinea("break");
        // Saltar al final del while
        grar.salto(etiquetas.snd);
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

        // El padre visita a las exprs. izq. y der. para generar la declaración de referencias
        super.visit(ob);

        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instCmpIR = ob.getInstruccionIR();
        String tipoIR = ob.getIzquierda().getTipo().getIR();
        String operadorParser = ob.getNombre();

        grar.setComentLinea(String.format("%s %s %s",
                ob.getIzquierda().toString(), ob.getNombre(), ob.getDerecha().toString()));

        if (ob instanceof OperacionBinariaAritmetica) {
            // Por ej.: %aux.ob.11 = add i32 %aux.sv.9, %aux.ref.10 ; %aux.ob.11 = %aux.sv.9 + %aux.ref.10
            grar.asig(refIR, instCmpIR, tipoIR, refIzqIR, refDerIR);
        } else if (ob instanceof Relacion) {
            // Por ej.: %aux.ob.15 = icmp sgt i32 %aux.sv.13, %aux.sv.14 ; %aux.sv.13 > %aux.sv.14
            grar.cmp(instCmpIR, refIR, tipoIR, refIzqIR, refDerIR);
        } else {
            throw new ExcepcionVisitor("Tipo de operación binaria inesperado.");
        }
    }

    @Override
    public void visit(NegacionAritmetica neg) throws ExcepcionVisitor {
        super.visit(neg);

        String refIR = Normalizador.crearNomRef("neg");
        neg.setRefIR(refIR);

        Expresion expr = neg.getExpresion();
        Tipo tipo = expr.getTipo();
        switch (tipo) {
            case INTEGER:
                // hacer 0 - expresion
                grar.asig(refIR, "sub", tipo.getIR(), "0", expr.getRefIR());
                break;
            case FLOAT:
                grar.asig(refIR, "fneg", tipo.getIR(), expr.getRefIR());
                break;
            default: throw new ExcepcionVisitor("Tipo de operación unaria inesperado: " + tipo);
        }

        // Ponerle esta referencia a la expresión
        // TODO: Por qué funciona sin esto?
        //expr.setRefIR(refIR);
    }

    @Override
    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        super.visit(neg);

        // Si el NOT es parte de una condición while por ejemplo, se invierten las etiquetas.
        // En ese caso no hay que generar una instrucción para negarla.
        // TODO: Terminar de revisar, seguro que me olvido algún caso
        if (neg.isEnCortocircuito()) return;

        String refIR = Normalizador.crearNomRef("neg");
        neg.setRefIR(refIR);

        Expresion expr = neg.getExpresion();
        grar.asig(refIR, "xor", "i1", "1", expr.getRefIR());
        expr.setRefIR(refIR);
    }

    @Override
    public void visit(OperacionConversion conv) throws ExcepcionVisitor {
        super.visit(conv);

        String refIR = Normalizador.crearNomRef("conv");
        conv.setRefIR(refIR);

        Expresion expr = conv.getExpresion();
        grar.ext(refIR, conv.getInstruccionIR(), expr.getRefIR(),
                expr.getTipo().getIR(), conv.getTipo().getIR());
        expr.setRefIR(refIR);
    }


    /* Valores */

    @Override
    public void visit(Literal lit) {
        /* Este visitor genera una variable auxiliar para utilizar los valores literales.
         * Como alternativa a generar la variable podríamos utilizar directamente el valor,
         * pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.
         */

        String refIR = Normalizador.crearNomRef("lit");
        lit.setRefIR(refIR);

        grar.setComentLinea(String.format("Literal %s", lit.getValor()));
        Tipo tipoParser = lit.getTipo();
        // Hack para generar referencias a valores en una línea (le sumo 0 al valor que quiero guardar)
        grar.suma(refIR, tipoParser.getIR(), lit.getValorIR(), tipoParser.getValorDefIR());
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

        grar.setComentLinea(String.format("Identificador %s", sv.getNombre()));
        grar.load(refIR, tipoIR, nombreIR);
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionVisitor {
        // El refIR que va a contener el valor de la invocación a la función
        String refIR = Normalizador.crearNomRef("invo");
        i.setRefIR(refIR);

        grar.setComentLinea(String.format("Invocación a %s()", i.getNombre()));

        // A las invocaciones a write las manejamos aparte
        if (i.getNombre().equals("write") || i.getNombre().equals("writeln")) {
            imprimirWrite(i);
            return;
        }
        // A las funciones read las definimos como funciones en IR, y las invocamos normalmente

        // Buscar la función en la tabla
        SimboloFuncion sf = tablaFunciones.get(i.getNombre());

        String nombreFun = sf.getNombreFuncionIR();
        String tipoFun = sf.getTipo().getIR();

        // Generar la lista de argumentos, además de visitarlos para generar las refs.
        // También se genera referencias para argumentos por defecto si son necesarios.
        String args = grarStrArgs(i.getArgs(), sf.getDeclaracion().getParams());
        grar.invocacion(nombreFun, tipoFun, refIR, args);
    }
}
