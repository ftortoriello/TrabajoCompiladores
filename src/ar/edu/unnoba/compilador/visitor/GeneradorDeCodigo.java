package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.OperacionBinariaAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Conjuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Disyuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
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

public class GeneradorDeCodigo extends Visitor {
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)
    // TODO: conversiones implícitas

    private StringBuilder codigo;
    private String nombreArchivoFuente;

    // Para imprimir o no los comentarios del recorrido, a veces molestan cuando se quiere ver el código
    private Boolean comentariosOn;

    private Map<String, SimboloFuncion> tablaFunciones;

    // Lista para guardar los nombres de las funciones que inicializan a las variables globales
    // Se añaden en el visit(DecVarIni), y se procesa una vez que se alcanza el main
    private List<String> varGblInit = new ArrayList<>();

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> TIPO_IR = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    /* Pila de pares de etiquetas para saber a donde saltar cuando se encuentra break o continue
     * dentro de un while.
     * La primer etiqueta del par representa el comienzo del bloque si la condición es verdadera,
     * y la segunda si es falsa (o el fin del bloque).
     */
    private Deque<Pair<String, String>> etiquetasMientras = new ArrayDeque<>();

    /* Pila de pares de etiquetas usada para el cortocircuito booleano */
    private Deque<Pair<String, String>> etiquetasOpBinLog = new ArrayDeque<>();

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

    // Genera un nombre único para una nueva etiqueta
    private String getNuevaEtiqueta(String nombreEtiqueta) {
        // TODO: Yo sacaría la e_:
        //return String.format("%s_%s", nombreEtiqueta, getID());
        // TODO: Estaría bueno que los ID sean por cada etiqueta y no globales??
        // (ej: inicio_while_1, bucle_while_1, inicio_while_2... se podría hacer con un mapa por ahí)
        return String.format("e_%s_%s", nombreEtiqueta, getID());
    }

    private void imprimirEtiqueta(String nombreEtiqueta) {
        codigo.append(String.format("\n%s:\n", nombreEtiqueta));
    }

    // TODO: función que imprima una línea con un comentario, así quedan alineados?

    private void grarCodSaltoInc(String etiquetaDestino) {
        codigo.append(String.format("\tbr label %%%s\n", etiquetaDestino));
    }

    private void grarCodSaltoCond(String cond, String etiquetaTrue, String etiquetaFalse) {
        codigo.append(String.format("\tbr i1 %s, label %%%s, label %%%s\n", cond, etiquetaTrue, etiquetaFalse));
    }

    private void grarComent(String coment) {
        if (comentariosOn) {
            codigo.append(String.format("\n\t; %s\n", coment));
        }
    }

    /* Devuelve una lista de parámetros de acuerdo a lo requerido por IR.
     * Utilizado para la declaración de funciones.
     */
    private String grarStrParams(List<Param> arrParams) {
        StringBuilder strParams = new StringBuilder();

        int cantParams = arrParams.size();
        for (int i = 0; i < cantParams; i++) {
            SimboloVariable simboloParam = (SimboloVariable) arrParams.get(i).getIdent();
            String tipoParam = TIPO_IR.get(simboloParam.getTipo()).fst;
            String paramNombreIR = simboloParam.getNombreIR();

            // Para separar los parámetros mediante comas, excepto el final
            String sep = i != cantParams - 1 ? ", " : "";

            // Añado el argumento a la lista
            strParams.append(String.format("%s %s%s", tipoParam, paramNombreIR, sep));
        }

        return strParams.toString();
    }

    private String grarStrArgs(List<Expresion> arrArgs) throws ExcepcionDeAlcance {
        // Similar grarStrParams, pero esta lista es utilizada por las invocaciones, o sea el
        // argumento puede ser una variable, un literal o una expresión más compleja, mientras
        // que en la declaración de la función eso va a ser siempre un objeto de tipo Param.

        StringBuilder strArgs = new StringBuilder();

        int cantArgs = arrArgs.size();

        for (int i = 0; i < cantArgs; i++) {

            Expresion exprArg = arrArgs.get(i);
            String tipoRetornoArg = TIPO_IR.get(exprArg.getTipo()).fst;
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

    /* Generar y apilar etiquetas de expresiones binarias lógicas.
     * Usado para el cortocircuito booleano. */
    private void grarEtiIniOpBinLog(String etiqueta) {
        String etiInicio = getNuevaEtiqueta("inicio_" + etiqueta);
        String etiFin = getNuevaEtiqueta("fin_" + etiqueta);
        etiquetasOpBinLog.push(new Pair<>(etiInicio, etiFin));

        grarCodSaltoInc(etiInicio);
        imprimirEtiqueta(etiInicio);
    }

    private void grarCortocircuitoBooleano(OperacionBinariaLogica ob) throws ExcepcionDeAlcance {
        // TODO: NegacionLogica. Habría que ver acá si el lado izquierdo es not? E ignorarlo después?

        Pair<String, String> etiquetas = etiquetasOpBinLog.peek();
        String etiVerdadero = etiquetas.fst;
        String etiFalso = etiquetas.snd;

        codigo.append(String.format("\t; Cortocircuito booleano: %s\n", ob));

        Expresion expIzquierda = ob.getIzquierda();
        expIzquierda.accept(this);

        final String etiTmp;
        if (ob instanceof Conjuncion) { // AND
            // Si el operador izquierdo es falso, esta operación es falsa
            etiTmp = getNuevaEtiqueta("and_verdadero");
            grarCodSaltoCond(expIzquierda.getRefIR(), etiTmp, etiFalso);
        } else if (ob instanceof Disyuncion) { // OR
            // Si el operador izquierdo es verdadero, esta operación es verdadera
            etiTmp = getNuevaEtiqueta("or_falso");
            grarCodSaltoCond(expIzquierda.getRefIR(), etiVerdadero, etiTmp);
        } else {
            throw new ExcepcionDeAlcance("Tipo de operación binaria lógica inesperado.");
        }

        imprimirEtiqueta(etiTmp);

        Expresion expDerecha = ob.getDerecha();
        expDerecha.accept(this);
        // asignar el nombre de la variable de la expresión derecha al resultado de la operación
        ob.setRefIR(expDerecha.getRefIR());
    }

    /*** Función principal ***/

    public String generarCodigo(Programa p, String nombreArchivoFuente, Boolean comentariosOn) throws ExcepcionDeAlcance {
        this.nombreArchivoFuente = nombreArchivoFuente;
        tablaFunciones = p.getTablaFunciones();
        this.comentariosOn = comentariosOn;

        super.procesar(p);
        return codigo.toString();
    }

    /**** Visitors ***/

    /* Base */

    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
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

        codigo.append("\n")
              .append("declare i32 @puts(i8*)\n")
              .append("declare i32 @printf(i8*, ...)\n")
              .append("\n")
              .append("@.true = private constant[4 x i8] c\".T.\\00\"\n")
              .append("@.false = private constant[4 x i8] c\".F.\\00\"\n")
              .append("\n")
              .append("@.integer = private constant [4 x i8] c\"%d\\0A\\00\"\n")
              .append("@.float = private constant [4 x i8] c\"%f\\0A\\00\"\n");

        super.visit(p);
    }
    
    @Override
    public void visit(Encabezado e) throws ExcepcionDeAlcance {
        super.visit(e);
    }

    @Override
    public void visit(Bloque b) throws ExcepcionDeAlcance {
        if (b.esProgramaPrincipal()) {
            codigo.append("\ndefine i32 @main(i32, i8**) {\n");
            varGblInit.forEach(fun -> {
                codigo.append(String.format("\tcall void %s()\n", fun));
            });
            super.visit(b);
            codigo.append("\tret i32 0\n");
            codigo.append("}\n");
        } else {
            super.visit(b);
        }
    }


    /* Sentencia de asignación */

    @Override
    public void visit(Asignacion asig) throws ExcepcionDeAlcance {
        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();

        Expresion exp = asig.getExpresion();
        // TODO
        boolean aplicarCortocircuito = (exp instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) grarEtiIniOpBinLog("asig");

        // Anexar declaración de referencias necesarias para la parte derecha de la asig.
        exp.accept(this);

        // origen va a contener la referencia al valor de la expresión
        String origen = exp.getRefIR();
        String tipoOrigen = TIPO_IR.get(exp.getTipo()).fst;
        String destino = svDestino.getNombreIR();
        String tipoDestino = TIPO_IR.get(svDestino.getTipo()).fst;
        // tipoOrigen y tipoDestino deberían ser iguales, pero lo dejo así para detectar algún error
        // y de paso usar los nombres de las variables para que quede un poco más claro lo que se hace

        grarComent(String.format("visit(Asignacion): %s = %s", svDestino.getNombre(), asig.getExpresion().toString()));

        codigo.append(String.format("\tstore %s %s, %s* %s\n", tipoOrigen, origen, tipoDestino, destino));

        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }


    /* Sentencias de declaración */

    @Override
    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        // Genera la declaración de una variable que no fue inicializada

        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        Boolean esGlobal = sv.getEsGlobal();
        String valorIR = TIPO_IR.get(dv.getTipo()).snd;

        grarComent(String.format("visit(DecVar): variable %s is %s = %s", sv.getNombre(), sv.getTipo(), valorIR));

        if (esGlobal) {
            codigo.append(String.format("\t%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
            codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        }
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionDeAlcance {
        // Genera la declaración de una variable que sí fue inicializada

        Expresion exp = dvi.getExpresion();
        boolean aplicarCortocircuito = (exp instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) grarEtiIniOpBinLog("decvarini");

        SimboloVariable sv = (SimboloVariable) dvi.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;

        if (sv.getEsGlobal()) {

            if (comentariosOn) {
                codigo.append(String.format("\n;visit(DecVarIni): variable %s is %s = %s",
                        sv.getNombre(), sv.getTipo(), dvi.getExpresion().toString()));
            }

            // Le asigno temporalmente a la var. el valor por defecto según
            // su tipo, porque no puedo inicializarla en el alcance global.
            String valorDef = TIPO_IR.get(sv.getTipo()).snd;
            codigo.append(String.format("\n%s = global %s %s\n", nombreIR, tipoIR, valorDef));

            // Creo una función que se va a llamar en el main para inicializar la var. con el valor correspondiente
            String nomFunAux = Normalizador.crearNomFun(String.format("init.var.gbl"));
            codigo.append(String.format("define void %s() {\n", nomFunAux));
            dvi.getExpresion().accept(this);
            String refIR = dvi.getExpresion().getRefIR();
            codigo.append(String.format("\tstore %1$s %2$s, %1s* %3$s\n", tipoIR, refIR, nombreIR));
            codigo.append("\tret void\n");
            codigo.append("}\n");

            // Guardo el nombre de la función para invocarla en el main
            varGblInit.add(nomFunAux);
        } else {
            // Visito a la expresión para generar la declaración de referencias necesarias
            exp.accept(this);

            // El refIR con el valor de la expresión viene resuelto gracias a la visita anterior
            String refIR;
            refIR = exp.getRefIR();

            grarComent(String.format("visit(DecVarIni): variable %s is %s = %s", sv.getNombre(), sv.getTipo(), refIR));

            codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
            codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, refIR));
        }

        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(DecFun df) throws ExcepcionDeAlcance {
        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo de retorno, nombre, parámetros y el cuerpo
        String funTipoRetIR = TIPO_IR.get(simboloFun.getTipo()).fst;
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
            // Para evitar comportamientos indefinidos, si la función no tiene retorno definido,
            // le genero uno en base al valor por defecto asociado a su tipo.
            String valorPorDef = TIPO_IR.get(df.getTipo()).snd;
            codigo.append(String.format("\tret %s %s\n", funTipoRetIR, valorPorDef));
        }

        codigo.append("}\n");
    }

    @Override
    public void visit(Param p) throws ExcepcionDeAlcance {
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
        String tipoIR = TIPO_IR.get(p.getTipo()).fst;

        sv.setRefIR(refIR);
        sv.setNombreIR(nombreIR);

        grarComent(String.format("visit(Param): %s", sv.getNombre()));

        codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
        codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\t; %1$s = %3$s\n", nombreIR, tipoIR, nombreOriginal));
    }

    @Override
    public void visit(ParamDef pi) throws ExcepcionDeAlcance {
        // El valor del parámetro ya debería venir resuelto desde la invocación,
        // por lo que puedo llamar a visit(Param) directamente.
        visit((Param) pi);
        // TODO etiquetas para el cortocircuito
    }


    /* Sentencias de selección */

    @Override
    public void visit(SiEntonces se) throws ExcepcionDeAlcance {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiFin = getNuevaEtiqueta("fin_if");
        etiquetasOpBinLog.push(new Pair<>(etiBlqThen, etiFin));

        grarComent("visit(SiEntonces)");

        // Salto condicional
        se.getCondicion().accept(this);
        String refCond = se.getCondicion().getRefIR();
        grarCodSaltoCond(refCond, etiBlqThen, etiFin);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        se.getBloqueSiEntonces().accept(this);
        grarCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);

        etiquetasOpBinLog.pop();
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiBlqElse = getNuevaEtiqueta("blq_else");
        String etiFin = getNuevaEtiqueta("fin_if");

        grarComent("visit(SiEntoncesSino)");

        // Salto condicional
        ses.getCondicion().accept(this);
        String refCond = ses.getCondicion().getRefIR();
        grarCodSaltoCond(refCond, etiBlqThen, etiBlqElse);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        ses.getBloqueSiEntonces().accept(this);
        grarCodSaltoInc(etiFin);

        // Caso false
        imprimirEtiqueta(etiBlqElse);
        ses.getBloqueSino().accept(this);
        grarCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);
    }


    /* Sentencias de iteración */

    @Override
    public void visit(Mientras m) throws ExcepcionDeAlcance {
        String etiInicioWhile = getNuevaEtiqueta("inicio_while");
        String etiBucleWhile = getNuevaEtiqueta("bucle_while");
        String etiFinWhile = getNuevaEtiqueta("fin_while");
        Pair<String, String> parEtiquetas = new Pair<>(etiBucleWhile, etiFinWhile);
        etiquetasMientras.push(parEtiquetas);

        grarComent("visit(While)");

        grarCodSaltoInc(etiInicioWhile);
        imprimirEtiqueta(etiInicioWhile);

        Expresion condicion = m.getCondicion();
        boolean aplicarCortocircuito = (condicion instanceof OperacionBinariaLogica);
        if (aplicarCortocircuito) etiquetasOpBinLog.push(parEtiquetas);

        // Generar ref. al resultado de la condición
        condicion.accept(this);
        String refCond = condicion.getRefIR();

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        // TODO: NegacionLogica
        if (condicion instanceof NegacionLogica) {
            grarCodSaltoCond(refCond, etiFinWhile, etiBucleWhile);
        } else {
            grarCodSaltoCond(refCond, etiBucleWhile, etiFinWhile);
        }
        imprimirEtiqueta(etiBucleWhile);

        // Generar cuerpo del while
        m.getBloqueSentencias().accept(this);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        grarCodSaltoInc(etiInicioWhile);

        imprimirEtiqueta(etiFinWhile);

        etiquetasMientras.pop();
        if (aplicarCortocircuito) etiquetasOpBinLog.pop();
    }


    /* Sentencias de control */

    @Override
    public void visit(Continuar c) throws ExcepcionDeAlcance {
        super.visit(c);
    }

    @Override
    public void visit(Retorno r) throws ExcepcionDeAlcance {
        // Generar refIR para la expresión de retorno
        r.getExpr().accept(this);

        String tipoRetorno = TIPO_IR.get(r.getExpr().getTipo()).fst;
        String refRetorno = r.getExpr().getRefIR();

        codigo.append(String.format("\tret %s %s\n", tipoRetorno, refRetorno));
    }

    @Override
    public void visit(Salir s) throws ExcepcionDeAlcance {
        super.visit(s);
    }


    /* Operaciones */

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        if (ob instanceof OperacionBinariaLogica) {
            // Manejar operaciones lógicas usando cortocircuito booleano.
            grarCortocircuitoBooleano((OperacionBinariaLogica) ob);
            return;
        }

        // Defino un nombre auxiliar con el cual puedo referenciar el valor de la expr.
        String refIR = Normalizador.crearNomRef("ob");
        ob.setRefIR(refIR);

        grarComent(String.format("visit(OperacionBinaria): %s %s %s",
                ob.getIzquierda().toString(), ob.getNombre(), ob.getDerecha().toString()));

        // El padre visita a las exprs. izq. y der. para generar la declaración de referencias
        super.visit(ob);

        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instIR = ob.getInstruccionIR();
        String tipoIR = TIPO_IR.get(ob.getIzquierda().getTipo()).fst;
        String operadorParser = ob.getNombre();

        if (ob instanceof OperacionBinariaAritmetica) {
            // Por ej.: %aux.ob.11 = add i32 %aux.sv.9, %aux.ref.10 ; %aux.ob.11 = %aux.sv.9 + %aux.ref.10
            codigo.append(String.format("\t%1$s = %2$s %3$s %4$s, %5$s\t; %1$s = %4$s %6$s %5$s\n",
                    refIR, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        } else if (ob instanceof Relacion) {
            String tipoCmp = ((Relacion) ob).getTipoCmp();
            // Por ej.: %aux.ob.15 = icmp sgt i32 %aux.sv.13, %aux.sv.14 ; %aux.sv.13 > %aux.sv.14
            codigo.append(String.format("\t%1$s = %2$s %3$s %4$s %5$s, %6$s ; %5$s %7$s %6$s\n",
                    refIR, tipoCmp, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        /* // implementado con cortocircuito booleano
        } else if (ob instanceof OperacionBinariaLogica) {
            // Por ej.: %aux.ob.3 = and i1 %aux.sv.1, %aux.sv.2
            codigo.append(String.format("\t%1$s = %2$s %3$s %4$s, %5$s ; %4$s %6$s %5$s\n",
                    refIR, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        */
        } else {
            throw new ExcepcionDeAlcance("Tipo de operación binaria inesperado.");
        }
    }

    @Override
    public void visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        super.visit(ou);
    }


    /* Valores */

    @Override
    public void visit(Literal lit) throws ExcepcionDeAlcance {
        // Este visitor genere una variable auxiliar para utilizar los valores literales
        // Como alternativa a generar la variable, podríamos guardar directamente el valor
        // pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.

        // TODO ver acá el tema ese de truncar los valores

        String refIR = Normalizador.crearNomRef("lit");
        lit.setRefIR(refIR);

        Tipo tipoParser = lit.getTipo();
        String tipoIR = TIPO_IR.get(tipoParser).fst;
        String valorParser = lit.getValor();
        String valorIR;

        if (tipoParser == Tipo.INTEGER) {
            valorIR = valorParser;
        } else if (tipoParser == Tipo.FLOAT) {
            valorIR = String.valueOf(valorParser);
        } else if (tipoParser == Tipo.BOOLEAN) {
            valorIR = valorParser.equals("false") ? "0" : "1";
        } else {
            throw new ExcepcionDeAlcance("Valor de tipo inesperado: " + lit.getTipo());
        }

        grarComent(String.format("visit(Literal): %s", valorParser));

        // Hack para generar referencias a valores en una línea (le sumo 0 al valor que quiero guardar)
        codigo.append(String.format("\t%s = add %s %s, 0\n", refIR, tipoIR, valorIR));
    }

    @Override
    public void visit(Identificador ident) {
        // Genera el store sobre un refIR para poder acceder a una variable

        // En esta etapa este Identificador va a ser siempre un SimboloVariable. Tengo que utilizarlo así porque
        // si creo visit(SimboloVariable) da muchos problemas (por ej. se rompe el graficado del AST)
        SimboloVariable sv = (SimboloVariable) ident;

        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        String refIR = Normalizador.crearNomRef("sv");
        sv.setRefIR(refIR);

        grarComent(String.format("visit(Identificador): %s", sv.getNombre()));

        codigo.append(String.format("\t%1$s = load %2$s, %2$s* %3$s\n", refIR, tipoIR, nombreIR));
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionDeAlcance {
        // TODO los nombres de los argumentos tienen que ser locales
        // o sea que si se pasa una variable global tiene generarse
        // una referencia y pasarse esto como parámetro

        if (i.getEsPredefinida()) {
            // TODO write o read
            // si las agregamos a la tabla de funciones, le asignamos un nombreIR y generamos la
            // declaración dinámicamente como si fueran una fun. más podemos eliminar este if creo
            codigo.append(String.format("\t; %s()\n", i.getNombre()));
            return;
        }

        SimboloFuncion sf = tablaFunciones.get(i.getNombre());

        String refIR = Normalizador.crearNomRef("fun");
        i.setRefIR(refIR);

        String tipoIR = TIPO_IR.get(sf.getTipo()).fst;
        String nombreIR = sf.getNombreIR();
        String args = grarStrArgs(i.getArgs());
        codigo.append(String.format("\t%s = call %s %s(%s)\n", refIR, tipoIR, nombreIR, args));
    }
}
