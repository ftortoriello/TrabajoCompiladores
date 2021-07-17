package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.Cadena;
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
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.util.GenerarIR;
import ar.edu.unnoba.compilador.util.Normalizador;
import ar.edu.unnoba.compilador.util.Etiquetas;

import java.io.*;
import java.util.*;

// TODO: Reemplazar funciones enCortoCircuito() por un flag en este Visitor

/**
 * Clase para generar código de LLVM IR a partir del AST.
 */
public class GeneradorDeCodigo extends Visitor {
    private String nombreArchivoFuente;

    private GenerarIR grar;

    /**
     * Tabla con las funciones definidas por el usuario.
     * Se extrae del objeto Programa al entrar en esta clase.
     */
    private Map<String, DecFun> tablaFunciones;

    /**
     * Lista para guardar los nombres de las funciones que inicializan a las variables globales.
     * Se añaden en el visit(DecVarIni), y se procesa una vez que se alcanza el main.
     */
    private final List<String> varGblInit = new ArrayList<>();

    /**
     * Arreglo con las cadenas utilizadas en el programa y que necesitamos declarar.
     * Se cargan en el visitor ReconocerCadenas sobre el objeto programa, y llegado
     * este visitor se inicializan en el ámbito global para poder ser usadas.
     */
    private List<Cadena> arrCadenas;

    /**
     * Pila de pares de etiquetas usada para el cortocircuito booleano
     * y para saber a donde saltar cuando se encuentra break o continue
     * dentro de un while. */
    private final Deque<Etiquetas> pilaEtiquetas = new ArrayDeque<>();

    // *** Funciones auxiliares ***

    public static boolean targetEsWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Función para no tener fija la arquitectura y SO destino.
     * El triple se puede sacar de  "llvm-config --host-target" o "clang -print-target-triple",
     * pero el datalayout no.
     * Compilar un programa básico en C a IR y obtenerlos.
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
        // Borrar archivo C temporal
        File file = new File("void.c");
        file.delete();
        if (datalayout == null || triple == null) {
            // Falló algo... tirar excepción para usar datos predeterminados
            throw new IOException("No se pudieron obtener los datos del host");
        }
        return datalayout + "\n" + triple;
    }

    /**
     * Generar invocación a write() o writeln() de un valor booleano.
     * Usado por imprimirWrite().
     */
    private void imprimiWriteBoolean(String refIR) {
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
    }

    /**
     * Generar invocación a write() o writeln().
     */
    private void imprimirWrite(InvocacionFuncion i) throws ExcepcionVisitor {
        Expresion arg = i.getArgs().get(0);
        arg.accept(this);
        String refIR = arg.getRefIR();

        grar.coment(i.getEtiqueta());

        if (arg instanceof Cadena) {
            Cadena cad = (Cadena) arg;
            grar.print(cad.getPtroIR(), cad.getLongitudIR());
        } else switch (arg.getTipo()) {
            case BOOLEAN -> imprimiWriteBoolean(refIR);
            case INTEGER -> grar.print("@.int_format", 3, "i32", refIR);
            case FLOAT   -> grar.print("@.double_print_format", 6, "double", refIR);
        }

        if (i.getNombre().equals("writeln")) {
            // Genero un salto de línea
            grar.print("@.salto_linea", 2);
        }
    }

    /**
     * Generar implementación de read_integer() o read_float(), dependiendo del tipo.
     */
    private void imprimirDefLeer(Tipo tipo) {
        // No hace falta generarles un id a los registros y etiquetas;
        // son únicas dentro de la función IR
        final String ptroValor = "%ptro.valor_leido";
        final String refValor = "%ref.valor_leido";
        final String tipoIR = tipo.getIR();

        grar.defFuncion("@read_" + tipo, tipoIR, "");
        grar.alloca(ptroValor, tipoIR);

        if (tipo.equals(Tipo.FLOAT)) {
            grar.scan("@.double_format", 4, tipoIR, ptroValor);
        } else {
            grar.scan("@.int_format", 3, tipoIR, ptroValor);
        }

        grar.load(refValor, tipoIR, ptroValor);
        grar.ret(tipoIR, refValor);
        grar.cierreBloque();
    }

    /**
     * Generar implementación de read_boolean().
     * Leemos caracteres. Si el primero es 't' o 'T' asumimos que es true.
     */
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

    /**
     * Devuelve una lista de parámetros de acuerdo a lo requerido por IR.
     * Utilizado para la declaración de funciones.
     */
    private String grarStrParams(List<Param> arrParams) {
        StringBuilder sbParams = new StringBuilder();

        int cantParams = arrParams.size();
        for (int i = 0; i < cantParams; i++) {
            SimboloVariable sv = (SimboloVariable) arrParams.get(i).getIdent();
            String tipoParam = sv.getTipo().getIR();
            String ptroParam = sv.getPtroIR();

            // Para separar los parámetros mediante comas, excepto el último
            String sep = i != cantParams - 1 ? ", " : "";

            // Añado el argumento a la lista
            sbParams.append(String.format("%s %s%s", tipoParam, ptroParam, sep));
        }

        return sbParams.toString();
    }

    /**
     * Similar a grarStrParams, pero esta lista es utilizada por las invocaciones, o sea el
     * argumento puede ser una variable, un literal o una expresión más compleja, mientras
     * que en la declaración de la función eso va a ser siempre un objeto de tipo Param.
     */
    private String grarStrArgs(List<Expresion> arrArgs, List<Param> paramsFormales) throws ExcepcionVisitor {
        StringBuilder sbArgs = new StringBuilder();

        for (int i = 0; i < paramsFormales.size(); i++) {
            // Si el argumento no existe, busco el parámetro formal para crear una ref. con el valor por defecto
            Expresion expr = i >= arrArgs.size() ?
                    ((ParamDef) paramsFormales.get(i)).getExpresion() :
                    arrArgs.get(i);

            // Evaluar la expr. y generar el refIR
            expr.accept(this);

            // Para separar los argumentos mediante comas, excepto el último
            String sep = i != paramsFormales.size() - 1 ? ", " : "";

            // Añado el argumento a la lista
            sbArgs.append(String.format("%s %s%s", expr.getTipo().getIR(), expr.getRefIR(), sep));
        }

        return sbArgs.toString();
    }

    /**
     * Genera el código del cortocircuito booleano para una expresión lógica
     * que es la cond. de una estructura (por ej. if).
     */
    private void imprimirCortocircuito(OperacionBinariaLogica ob) throws ExcepcionVisitor {
        grar.coment(String.format("Cortocircuito booleano: %s", ob));

        final Etiquetas eti = pilaEtiquetas.peek();
        final String etiVerdadero = eti.getSaltoPrimera();
        final String etiFalso = eti.getSaltoSegunda();

        // Esta etiqueta se utiliza cuando es necesario seguir comparando para
        // determinar el valor de verdad de la expresión.
        final String etiContCmp;
        if (ob instanceof Conjuncion) {
            // AND: Si el op. izq. es falso, esta expr. es falsa y corto. Sino, sigo comparando.
            etiContCmp = Normalizador.crearNomEtiqueta("izq_and_t");
            pilaEtiquetas.push(new Etiquetas(etiContCmp, etiFalso));
        } else if (ob instanceof Disyuncion) {
            // OR: Si el op. izq. es verdadero, esta expr. es verdadera y corto. Sino, sigo comparando.
            etiContCmp = Normalizador.crearNomEtiqueta("izq_or_f");
            pilaEtiquetas.push(new Etiquetas(etiVerdadero, etiContCmp));
        } else {
            throw new ExcepcionVisitor(ob, "Tipo de operación binaria lógica inesperado.");
        }

        Expresion expIzquierda = ob.getIzquierda();

        // Visitar el operando de la izquierda para generar sus declaraciones y crear su refIR
        if (expIzquierda instanceof OperacionBinariaLogica || expIzquierda instanceof NegacionLogica) {
            expIzquierda.setEnCortocircuito(true);
        }
        expIzquierda.accept(this);

        // Actualizar las etiquetas porque podrían haberse invertido en el accept de expIzquierda.
        // Desapilarlas para que la expr. derecha pueda utilizar las etiquetas que le corresponden.
        grar.salto(expIzquierda.getRefIR(), pilaEtiquetas.pop());
        grar.etiqueta(etiContCmp);

        Expresion expDerecha = ob.getDerecha();
        if (expDerecha instanceof OperacionBinariaLogica || expDerecha instanceof NegacionLogica) {
            expDerecha.setEnCortocircuito(true);
        }
        expDerecha.accept(this);

        // Asignar el nombre de la variable de la expresión derecha al resultado de la operación
        ob.setRefIR(expDerecha.getRefIR());
    }

    /**
         * Invocar a las funciones que asignan a las variables
         * globales el valor con el que fueron declaradas.
         */
    private void inicializarVarsGbls() {
        varGblInit.forEach(fun -> grar.invocacion(fun, ""));
    }

    /**
     * Inicializar las cadenas que van a usarse en el programa.
     */
    private void declararVarsStrs() {
        // Por ej.: @ptro.str.2 = private constant [5 x i8] c"Hola\00"
        arrCadenas.forEach(cad ->
                grar.cadena(cad.getPtroIR(), cad.getValorIR(), cad.getLongitudIR()));
    }


    /**
     * Función principal.
     */
    public String generarCodigo(Programa p, String nombreArchivoFuente) throws ExcepcionVisitor {
        this.nombreArchivoFuente = nombreArchivoFuente;
        this.tablaFunciones = p.getTablaFunciones();
        this.arrCadenas = p.getArrCadenas();

        grar = new GenerarIR();
        super.procesar(p);
        return grar.getCodigo();
    }

    // *** Visitors ***

    // Base
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
                target = """
                        target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
                        target triple = "x86_64-pc-windows-msvc19.28.29335"
                        """;
            } else {
                // Target Franco
                target = """
                        target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
                        target triple = "x86_64-pc-linux-gnu"
                        """;
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

        grar.encabezado(sb.toString());

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

    // *** Sentencia de asignación ***
    @Override
    public void visit(Asignacion asig) throws ExcepcionVisitor {
        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();
        Expresion expr = asig.getExpresion();

        // Generar declaración de referencias necesarias para la parte derecha de la asig.
        expr.accept(this);

        // Origen va a contener la referencia al valor de la expresión
        String refIR = expr.getRefIR();
        String destino = svDestino.getPtroIR();
        String tipoIR = svDestino.getTipo().getIR();

        grar.setComentLinea(String.format("%s %s %s",
                svDestino.getNombre(), asig.getEtiqueta(), expr));

        // Generar la asignación
        grar.store(destino, tipoIR, refIR);
    }


    // *** Sentencias de declaración ***

    // Genera la declaración de una variable que no fue inicializada
    @Override
    public void visit(DecVar dv) {
        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String ptroIR = sv.getPtroIR();
        String tipoIR = sv.getTipo().getIR();
        Boolean esGlobal = sv.getEsGlobal();
        String valorDef = dv.getTipo().getValorDef();

        grar.setComentLinea(String.format("variable %s is %s = %s", sv.getNombre(), sv.getTipo(), valorDef));

        if (esGlobal) {
            grar.global(ptroIR, tipoIR, valorDef);
        } else {
            grar.alloca(ptroIR, tipoIR);
            grar.store(ptroIR, tipoIR, valorDef);
        }
    }

    // Genera la declaración de una variable que fue inicializada
    @Override
    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        Expresion expr = dvi.getExpresion();
        SimboloVariable sv = (SimboloVariable) dvi.getIdent();

        // Parámetros que necesito para declarar la variable
        String ptroIR = sv.getPtroIR();
        String tipoIR = sv.getTipo().getIR();
        grar.setComentLinea(String.format("variable %s is %s = %s",
                sv.getNombre(), sv.getTipo(), expr));

        if (sv.getEsGlobal()) {
            // Le asigno temporalmente a la var. el valor por defecto según su tipo, porque
            // no puedo inicializarla con una ref. en el alcance global.
            String valorDef = sv.getTipo().getValorDef();
            grar.global(ptroIR, tipoIR, valorDef);

            // Creo una función que se va a llamar en el main para asignar en la var. el
            // valor correspondiente
            String nomFunAux = Normalizador.crearNomFun("init.var.gbl");
            grar.defFuncion(nomFunAux, "void", "");

            expr.accept(this);
            String refIR = expr.getRefIR();

            grar.store(ptroIR, tipoIR, refIR);

            // Guardo el nombre de la función para invocarla en el main
            varGblInit.add(nomFunAux);
            grar.ret();
            grar.cierreBloque();
        } else {
            // Sino, es una variable local y el valor puede guardarse directamente
            grar.alloca(ptroIR, tipoIR);

            grar.setComentLinea(String.format("variable %s is %s = %s",
                    sv.getNombre(), sv.getTipo(), expr));
            expr.accept(this);

            // El refIR con el valor de la expresión viene resuelto gracias a la visita anterior
            String refIR = expr.getRefIR();
            grar.store(ptroIR, tipoIR, refIR);
        }
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        String tipoRet = df.getTipo().getIR();
        String nombreFun = df.getNombreFunIR();
        String ptroRet = df.getPtroRet();
        String refRet = df.getRefIR();
        String etiquetaFin = df.getEtiquetaFin();
        String valorPorDef = df.getTipo().getValorDef();

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        String params = grarStrParams(df.getParams());
        grar.defFuncion(nombreFun, tipoRet, params);

        // Inicializo el retorno de la función con el valor por defecto
        grar.alloca(ptroRet, tipoRet);
        grar.store(ptroRet, tipoRet, valorPorDef);

        // Generar referencias de los parámetros
        for (Param param : df.getParams()) {
            param.accept(this);
        }

        // Generar cuerpo de la función
        df.getBloque().accept(this);

        // Tengo un único retorno para toda la función. Si no se pisó el valor de retorno
        // en algun visit(Retorno), para evitar comportamientos indefinidos se devuelve el
        // valor por defecto que fue asignado al inicio de la declaración.
        grar.salto(etiquetaFin);
        grar.etiqueta(etiquetaFin);

        // Cargo el valor de retorno de la función
        grar.load(refRet, tipoRet, ptroRet);
        grar.ret(tipoRet, refRet);
        grar.cierreBloque();
    }

    @Override
    public void visit(Param p) {
        /*
         * Para poder utilizar el parámetro creo una variable auxiliar, para la cual genero un
         * ptroIR y un refIR, que pisan al que viene en el objeto SimboloVariable del parámetro.
         * Después guardo el valor que viene en el parámetro en esta "nueva" var.
         * Esto supone que el pasaje es por valor y no por referencia.
         * Si el parámetro fue inicializado por defecto o no es indistinto.
         */
        SimboloVariable sv = (SimboloVariable) p.getIdent();

        // Guardo el nombre original del parámetro para poder extraer el valor, después lo piso
        String nombreOriginal = sv.getPtroIR();
        String tipoIR = p.getTipo().getIR();

        sv.setPtroIR(Normalizador.crearNomPtroLcl("aux." + sv.getNombre()));
        sv.setRefIR(Normalizador.crearNomPtroLcl(sv.getNombre()));

        grar.alloca(sv.getPtroIR(), tipoIR);
        grar.setComentLinea(String.format("Param %s", sv.getNombre()));
        grar.store(sv.getPtroIR(), tipoIR, nombreOriginal);
    }

    @Override
    public void visit(ParamDef pi) {
        // El valor del parámetro ya debería venir resuelto desde la invocación,
        // por lo que puedo llamar a visit(Param) directamente.
        visit((Param) pi);
    }


    // *** Sentencias de selección ***

    @Override
    public void visit(SiEntonces se) throws ExcepcionVisitor {
        grar.coment("if / then");

        String etiBlqThen = Normalizador.crearNomEtiqueta("blq_then");
        String etiFin = Normalizador.crearNomEtiqueta("fin_if");

        Expresion cond = se.getCondicion();
        if (cond instanceof OperacionBinariaLogica || cond instanceof NegacionLogica) {
            cond.setEnCortocircuito(true);
        }

        pilaEtiquetas.push(new Etiquetas(etiBlqThen, etiFin));
        cond.accept(this);

        // Salto condicional
        String refCond = cond.getRefIR();
        grar.salto(refCond, pilaEtiquetas.peek());
        pilaEtiquetas.pop();

        // Caso true
        grar.etiqueta(etiBlqThen);
        se.getBloqueSiEntonces().accept(this);
        grar.salto(etiFin);

        // Fin if
        grar.etiqueta(etiFin);
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        grar.coment("if / then / else");

        String etiBlqThen = Normalizador.crearNomEtiqueta("blq_then");
        String etiBlqElse = Normalizador.crearNomEtiqueta("blq_else");
        String etiFin = Normalizador.crearNomEtiqueta("fin_if");

        Expresion cond = ses.getCondicion();
        if (cond instanceof OperacionBinariaLogica || cond instanceof NegacionLogica) {
            cond.setEnCortocircuito(true);
        }

        pilaEtiquetas.push(new Etiquetas(etiBlqThen, etiBlqElse));
        cond.accept(this);

        String refCond = ses.getCondicion().getRefIR();
        // Salto condicional
        grar.salto(refCond, pilaEtiquetas.peek());
        pilaEtiquetas.pop();

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


    // *** Sentencias de iteración ***

    @Override
    public void visit(Mientras m) throws ExcepcionVisitor {
        grar.coment("while");

        String etiInicioWhile = Normalizador.crearNomEtiqueta("inicio_while");
        String etiBucleWhile = Normalizador.crearNomEtiqueta("bucle_while");
        String etiFinWhile = Normalizador.crearNomEtiqueta("fin_while");
        Etiquetas parEtiquetas = new Etiquetas(etiBucleWhile, etiFinWhile);

        String etiIterador = null;
        if (m.eraFor()) {
            // Si este while era originalmente un for, se crea una etiqueta especial en la que en cada
            // iteración siempre se incrementa el valor de la variable sobre la que se está iterando
            etiIterador = Normalizador.crearNomEtiqueta("inc_iterador");
            parEtiquetas.setEtiContinuar(etiIterador);
        } else {
            parEtiquetas.setEtiContinuar(etiInicioWhile);
        }

        grar.salto(etiInicioWhile);
        grar.etiqueta(etiInicioWhile);

        pilaEtiquetas.push(parEtiquetas);

        Expresion cond = m.getCondicion();
        if (cond instanceof OperacionBinariaLogica || cond instanceof NegacionLogica) {
            cond.setEnCortocircuito(true);
        }
        cond.accept(this);

        // Se evalúa la condición, si es verdadera se salta al bucle, sino al fin
        // Tomo las etiquetas de la pila porque podrían haber sido invertidas en el visit de la expr.
        grar.salto(cond.getRefIR(), pilaEtiquetas.peek());
        grar.etiqueta(etiBucleWhile);

        // Generar cuerpo del while
        if (m.eraFor()) {
            // Aceptar todas las sentencias excepto la última,
            // porque hay que insertarle una etiqueta antes
            List<Nodo> sentencias = m.getBloqueSentencias().getSentencias();
            for (Nodo sentencia : sentencias.subList(0, sentencias.size() - 1)) {
                sentencia.accept(this);
            }
            grar.salto(etiIterador);
            grar.etiqueta(etiIterador);
            // Aceptar la última
            sentencias.get(sentencias.size() - 1).accept(this);
        } else {
            m.getBloqueSentencias().accept(this);
        }

        pilaEtiquetas.pop();

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        grar.salto(etiInicioWhile);
        grar.etiqueta(etiFinWhile);
    }


    // *** Sentencias de control ***

    @Override
    public void visit(Retorno r) throws ExcepcionVisitor {
        DecFun df = r.getFun();
        Expresion expr = r.getExpresion();

        // Generar refIR para la expresión de retorno
        expr.accept(this);

        grar.setComentLinea("return");

        String tipoIR = df.getTipo().getIR();
        // La variable con el valor de la expresión resuelto
        String refExpr = expr.getRefIR();
        // El puntero del cual se toma el valor a retornar.
        String ptroRet = df.getPtroRet();

        // Guardo el valor de la expr. del return en la variable que se va a retornar al final de la función
        grar.store(ptroRet, tipoIR, refExpr);

        // Salto al final de la función
        grar.salto(df.getEtiquetaFin());
    }

    @Override
    public void visit(Continuar c) {
        grar.setComentLinea("continue");
        // Saltar a la evaluación de la condición del while, o al incremento del iterador del for
        grar.salto(pilaEtiquetas.peek().getSaltoContinuar());
    }

    @Override
    public void visit(Salir s) {
        grar.setComentLinea("break");
        // Saltar al final del while
        grar.salto(pilaEtiquetas.peek().getSaltoSegunda());
    }


    // *** Operaciones ***

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionVisitor {
        ob.setRefIR(Normalizador.crearNomRef("ob"));

        if (ob.getEnCortocircuito()) {
            // Las operaciones binarias que sean la condición de una estructura (por ej. un if)
            // y pertenezcan a la clase OperacionBinariaLogica tendrán este atributo en true.
            imprimirCortocircuito((OperacionBinariaLogica) ob);
            return;
        }

        // El padre visita a las exprs. izq. y der. para generar la declaración de referencias
        super.visit(ob);

        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instCmpIR = ob.getInstruccionIR();
        String tipoIR = ob.getIzquierda().getTipo().getIR();

        grar.setComentLinea(String.format("%s %s %s",
                ob.getIzquierda().toString(), ob.getNombre(), ob.getDerecha().toString()));

        if (ob instanceof OperacionBinariaAritmetica || ob instanceof OperacionBinariaLogica) {
            // Por ej.: %aux.ob.11 = add i32 %aux.sv.9, %aux.ref.10 ; %aux.ob.11 = %aux.sv.9 + %aux.ref.10
            grar.asig(ob.getRefIR(), instCmpIR, tipoIR, refIzqIR, refDerIR);
        } else if (ob instanceof Relacion) {
            // Por ej.: %aux.ob.15 = icmp sgt i32 %aux.sv.13, %aux.sv.14 ; %aux.sv.13 > %aux.sv.14
            grar.cmp(instCmpIR, ob.getRefIR(), tipoIR, refIzqIR, refDerIR);
        } else {
            throw new ExcepcionVisitor(ob, "Tipo de operación binaria inesperado.");
        }
    }

    @Override
    public void visit(NegacionAritmetica neg) throws ExcepcionVisitor {
        super.visit(neg);

        neg.setRefIR(Normalizador.crearNomRef("neg"));

        Expresion expr = neg.getExpresion();
        Tipo tipo = expr.getTipo();
        switch (tipo) {
            // Hacer 0 - expresion
            case INTEGER -> grar.asig(neg.getRefIR(), "sub", tipo.getIR(), "0", expr.getRefIR());
            case FLOAT   -> grar.asig(neg.getRefIR(), "fneg", tipo.getIR(), expr.getRefIR());
            default -> throw new ExcepcionVisitor(neg, "Tipo inesperado: " + tipo);
        }
    }

    @Override
    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        super.visit(neg);
        String refExpr = neg.getExpresion().getRefIR();

        // Si el NOT es por ejemplo parte de una condición while, nos limitamos a invertir
        // las etiquetas ya que podemos omitir la operación de negación.
        if (neg.getEnCortocircuito()) {
            neg.setRefIR(refExpr);
            pilaEtiquetas.peek().invertirSalto();
            grar.coment(String.format("Cortocircuito booleano: %s (se invirtieron las etiquetas)", neg));
            return;
        }


        neg.setRefIR(Normalizador.crearNomRef("not"));
        String refNeg = neg.getRefIR();
        grar.asig(refNeg, "xor", "i1", "1", refExpr);
    }

    @Override
    public void visit(OperacionConversion conv) throws ExcepcionVisitor {
        super.visit(conv);

        conv.setRefIR(Normalizador.crearNomRef("conv"));
        Expresion expr = conv.getExpresion();

        // Convertimos el valor al tipo indicado en OperacionConversion
        grar.ext(conv.getRefIR(), conv.getInstruccionIR(), expr.getRefIR(),
                expr.getTipo().getIR(), conv.getTipo().getIR());
        expr.setRefIR(conv.getRefIR());
    }


    // *** Valores ***

    @Override
    public void visit(Literal lit) {
        // Utilizamos una ref. auxiliar para almacenar los valores literales. Como
        // alternativa podríamos utilizar directamente el valor, pero de esta manera
        // queda más uniforme con el resto del código.
        grar.setComentLinea(String.format("Literal %s", lit));

        lit.setRefIR(Normalizador.crearNomRef(lit.toString()));
        // Hack para generar referencias a valores en una línea (le sumo 0 al valor que quiero guardar)
        grar.suma(lit.getRefIR(), lit.getTipo().getIR(),
                lit.toString(), lit.getTipo().getValorDef());
    }

    @Override
    public void visit(Identificador ident) {
        // Identificador va a ser siempre un SimboloVariable.
        SimboloVariable sv = (SimboloVariable) ident;

        grar.setComentLinea(String.format("Variable %s", sv.getNombre()));

        // Generar referencias dinámicamente cada vez que se usa la variable
        // Necesitamos esto por el Static Single Assignment
        sv.setRefIR(Normalizador.crearNomRef(ident.getNombre()));

        // Genera el store sobre el refIR del SimboloVariable para luego acceder al valor
        grar.load(sv.getRefIR(), sv.getTipo().getIR(), sv.getPtroIR());
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionVisitor {
        // A las invocaciones a write las manejamos aparte
        if (i.getNombre().equals("write") || i.getNombre().equals("writeln")) {
            imprimirWrite(i);
            return;
        }
        // Y a las funciones read las definimos como funciones en IR, y las invocamos normalmente

        i.setRefIR(Normalizador.crearNomRef("invo"));

        // Buscar la función en la tabla
        DecFun df = tablaFunciones.get(i.getNombre());

        String nombreFun = df.getNombreFunIR();
        String tipoFun = df.getTipo().getIR();

        grar.setComentLinea(String.format("Invocación a %s()", i.getNombre()));

        // Generar la lista de argumentos, además de visitarlos para generar las refs.
        // También se genera referencias para argumentos por defecto si son necesarios.
        String args = grarStrArgs(i.getArgs(), df.getParams());
        grar.invocacion(nombreFun, tipoFun, i.getRefIR(), args);
    }
}
