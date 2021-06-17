package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.OperacionBinariaAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import jflex.base.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorDeCodigo extends Visitor<String> {
    // TODO: esta clase hay que revisarla toda, la adapté rápido del ejemplo original para que compile
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)

    private String nombreArchivoFuente;

    private Map<String, SimboloFuncion> tablaFunciones;

    // *** AUXILIARES ***

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> TIPO_IR = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    public String procesar(Programa p, String nombreArchivoFuente) throws ExcepcionDeAlcance {
        this.nombreArchivoFuente = nombreArchivoFuente;
        tablaFunciones = p.getTablaFunciones();

        String resultado = visit(p);
        return resultado;
    }

    /* Función para no tener fija la arquitectura y SO destino
     * Es un hack, pero mejor que poner ifs...
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
    private String getNuevaEtiqueta(String nombre) {
        return String.format("e_%s_%s", nombre, getID());
    }

    private String formatearEtiqueta(String nombre) {
        return String.format("\n%s:\n", nombre);
    }

    private String grarCodSaltoInc(String etiquetaDestino) {
        return String.format("br label %%%s\n", etiquetaDestino);
    }

    private String grarCodSaltoCond(String cond, String etiquetaTrue, String etiquetaFalse) {
        return String.format("br %s, label %%%s, label %%%s\n", cond, etiquetaTrue, etiquetaFalse);
    }

    // *** VISITORS ***

    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(String.format("; Programa: %s\n", p.getNombre()));
        resultado.append(String.format("source_filename = \"%s\"\n", nombreArchivoFuente));

        try {
            resultado.append(getHostTarget());
        } catch (IOException | InterruptedException e) {
            // algo falló... dejarlo hardcodeado
            if (System.getProperty("os.name").startsWith("Windows")) {
                // Target Bruno
                resultado.append("target datalayout = \"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n");
                resultado.append("target triple = \"x86_64-pc-windows-msvc19.28.29335\"\n");
            } else {
                // Target Franco
                resultado.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n");
                resultado.append("target triple = \"x86_64-pc-linux-gnu\"\n");
            }
        }

        resultado.append("\n");
        resultado.append("declare i32 @puts(i8*)\n");
        resultado.append("declare i32 @printf(i8*, ...)\n");
        resultado.append("\n");
        resultado.append("@.true = private constant[4 x i8] c\".T.\\00\"\n");
        resultado.append("@.false = private constant[4 x i8] c\".F.\\00\"\n");
        resultado.append("\n");
        resultado.append("@.integer = private constant [4 x i8] c\"%d\\0A\\00\"\n");
        resultado.append("@.float = private constant [4 x i8] c\"%f\\0A\\00\"\n");
        resultado.append(super.visit(p));
        return resultado.toString();
    }

    @Override
    public String visit(Identificador ident) {
        // Genera el store sobre un refIR para poder acceder a una variable

        // En esta etapa este Identificador va a ser siempre un SimboloVariable. Tengo que utilizarlo así porque
        // si creo visit(SimboloVariable) da muchos problemas (por ej. se rompe el graficado del AST)
        SimboloVariable sv = (SimboloVariable) ident;

        StringBuilder resultado = new StringBuilder();

        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        String refIR = Normalizador.getNvoNomVarAux("sv");
        sv.setRefIR(refIR);

        resultado.append(String.format("; visit(SimboloVariable %s)\n", sv.getNombre())); // borrar desp. es para ver si hace cagadas
        resultado.append(String.format("%1$s = load %2$s, %2$s* %3$s\n", refIR, tipoIR, nombreIR));
        return resultado.toString();
    }

    @Override
    public String visit(Literal lit) throws ExcepcionDeAlcance {
        // Este visitor genere una variable auxiliar para utilizar los valores literales
        // Como alternativa a generar la variable, podríamos guardar el valor en refIR
        // pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.

        // TODO ver acá el tema ese de truncar los valores

        String refIR = Normalizador.getNvoNomVarAux("ref");
        Tipo tipoParser = lit.getTipo();
        String tipoIR = TIPO_IR.get(tipoParser).fst;
        String valorParser = lit.getValor();
        String valorIR;

        if (tipoParser == Tipo.INTEGER) {
            valorIR = valorParser;
        } else if (tipoParser == Tipo.FLOAT) {
            valorIR = String.valueOf(valorParser);
        } else if (tipoParser == Tipo.BOOLEAN) {
            valorIR = valorParser == "false" ? "0" : "1";
        } else {
            throw new ExcepcionDeAlcance("Valor de tipo inesperado: " + lit.getTipo());
        }

        lit.setRefIR(refIR);

        StringBuilder resultado = new StringBuilder();
        resultado.append(String.format("\n; visit(Literal %s)\n", valorParser));
        resultado.append(String.format("%s = add %s %s, 0\n", refIR, tipoIR, valorIR));
        return resultado.toString();
    }

    // *** PROCESOS ***

    protected String procesarPrograma(Programa p, String enc, String cpo) {
        return enc + cpo;
    }

    protected String procesarEncabezado(Encabezado encabezado, List<String> sentencias) {
        // Retorno solo la lista de sentencias
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach(resultado::append);
        return resultado.toString();
    }

    protected String procesarBloque(Bloque bloque, List<String> sentencias) {
        StringBuilder resultado = new StringBuilder();

        if (bloque.esProgramaPrincipal()) {
            resultado.append("\ndefine i32 @main(i32, i8**) {\n");
            sentencias.forEach((sentencia) -> {
                resultado.append(sentencia);
            });
            resultado.append("ret i32 0\n");
            resultado.append("}\n\n");
        } else {
            sentencias.forEach((sentencia) -> {
                resultado.append(sentencia);
            });
        }

        return resultado.toString();
    }

    protected String procesarDecFuncion(DecFuncion df, List<String> args, String cuerpo) {

        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo, nombre y parámetros
        String tipoRetorno = TIPO_IR.get(simboloFun.getTipo()).fst;

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < df.getArgs().size(); i++) {
            SimboloVariable sArg = (SimboloVariable) df.getArgs().get(i).getIdent();

            String tipoRetornoArg = TIPO_IR.get(sArg.getTipo()).fst;
            String nombreIR = sArg.getNombreIR();

            String sep = i != df.getArgs().size() - 1 ? ", " : "";
            params.append(String.format("%s %s%s", tipoRetornoArg, nombreIR, sep));
        }

        // Definir la función
        StringBuilder declaracionFunIR = new StringBuilder();
        declaracionFunIR.append(String.format("\ndefine %s @%s(%s) {\n", tipoRetorno, simboloFun.getNombreIR(), params));
        declaracionFunIR.append(cuerpo);
        declaracionFunIR.append("}\n\n");

        return declaracionFunIR.toString();
    }

    protected String procesarDecVar(DecVar dv, String sinUso) {
        // Genera la declaración de una variable que no fue inicializada

        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        Boolean esGlobal = sv.getEsGlobal();
        String valorIR = TIPO_IR.get(dv.getTipo()).snd;

        StringBuilder resultado = new StringBuilder();

        // Mostrar comentario con la declaración en el lenguaje original
        resultado.append(String.format("\n; procesarDecVar: variable %s is %s = %s\n",
                sv.getNombre(), sv.getTipo(), valorIR));

        if (esGlobal) {
            resultado.append(String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            resultado.append(String.format("%s = alloca %s\n", nombreIR, tipoIR));
            resultado.append(String.format("store %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        }

        resultado.append("\n");
        return resultado.toString();
    }

    protected String procesarDecVarInicializada(DecVarInicializada dvi, String sinUso, String decsRefs) {
        // Genera la declaración de una variable que sí fue inicializada

        SimboloVariable sv = (SimboloVariable) dvi.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        Boolean esGlobal = sv.getEsGlobal();
        String valorIR = "";

        StringBuilder resultado = new StringBuilder();

        if (esGlobal) {
            // TODO
            // Tengo que armar una función que me retorne el valor para poder asignarla
            // valorIR = el return de la funcion que hay que crear
            valorIR = "globalIni";
        } else {
            // decsRefs contiene las declaraciones que necesito para acceder al valor de la expresión
            resultado.append(decsRefs);
            // El resultado final del valor de la expresión ya viene seteado gracias a los visitors
            valorIR = dvi.getExpresion().getRefIR();
        }


        // Mostrar comentario con la declaración en el lenguaje original
        resultado.append(String.format("\n; procesarDecVarInicializada: variable %s is %s = %s\n",
                sv.getNombre(), sv.getTipo(), valorIR));

        if (sv.getEsGlobal()) {
            resultado.append(String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            resultado.append(String.format("%s = alloca %s\n", nombreIR, tipoIR));
            resultado.append(String.format("store %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        }

        resultado.append("\n");
        return resultado.toString();
    }

    protected String procesarAsignacion(Asignacion asig, String sinUso, String decRefs) {
        // FIXME: acá no funcionan las conversiones implícitas
        StringBuilder resultado = new StringBuilder();

        // Anexo la generación de las vars. auxs. necesarias
        resultado.append(decRefs);

        // Elementos que necesito para definir la asignación
        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();
        String origen = asig.getExpresion().getRefIR();
        String tipoOrigen = TIPO_IR.get(asig.getExpresion().getTipo()).fst;
        String destino = svDestino.getNombreIR();
        String tipoDestino = TIPO_IR.get(svDestino.getTipo()).fst;
        // tipoOrigen y tipoDestino deberían ser iguales, pero lo dejo así para detectar algún error
        // y de paso usar los nombres de las variables para que quede un poco más claro lo que se hace

        resultado.append(String.format("\n; procesarAsignacion\n"));
        resultado.append(String.format("store %1$s %2$s, %3$s* %4$s ; %4$s = %2$s\n",
                tipoOrigen, origen, tipoDestino, destino));
        return resultado.toString();
    }

    protected String procesarOperacionBinaria(OperacionBinaria ob, String decAuxIzq, String decAuxDer) {
        StringBuilder resultado = new StringBuilder();

        // Defino un nombre auxiliar con el cual puedo referenciar el valor de la expr.
        String refIR = Normalizador.getNvoNomVarAux("ob");
        ob.setRefIR(refIR);
        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instIR = ob.getInstruccionIR();
        String tipoIR = TIPO_IR.get(ob.getIzquierda().getTipo()).fst;
        String operadorParser = ob.getNombre();

        // En decAuxIzq y decAuxDer vienen las declaraciones de variables auxiliares que voy a necesitar
        resultado.append(decAuxIzq);
        resultado.append(decAuxDer);

        if (ob instanceof OperacionBinariaAritmetica) {
            resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s ; %1$s = %4$s %6$s %5$s\n",
                    refIR, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        } else if (ob instanceof Relacion) {
            resultado.append(String.format("%1$s = icmp %2$s %3$s %4$s, %5$s ; %4$s %6$s %5$s)",
                    refIR, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        } else if (ob instanceof OperacionBinariaLogica) {
            resultado.append("; procesarOperacionBinaria -> OpBinLog sin implementar");
        }

        return resultado.toString();
    }

    protected String procesarInvocacionFuncion(InvocacionFuncion invoFun) {
        // TODO
        return String.format("; Invocación a %s()\n", invoFun.getNombre());
    }

    protected String procesarSiEntonces(String cond, String blqSi) {
        StringBuilder resultado = new StringBuilder();

        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiFin = getNuevaEtiqueta("fin_if");

        // Salto condicional
        resultado.append(grarCodSaltoCond(cond, etiBlqThen, etiFin));

        // Caso true
        resultado.append(formatearEtiqueta(etiBlqThen));
        resultado.append(blqSi);
        resultado.append(grarCodSaltoInc(etiFin));

        // Fin if
        resultado.append(formatearEtiqueta(etiFin));

        return resultado.toString();
    }

    protected String procesarSiEntoncesSino(String cond, String blqSi, String blqSino) {
        StringBuilder resultado = new StringBuilder();

        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiBlqElse = getNuevaEtiqueta("blq_else");
        String etiFin = getNuevaEtiqueta("fin_if");

        // Salto condicional
        resultado.append(grarCodSaltoCond(cond, etiBlqThen, etiBlqElse));

        // Caso true
        resultado.append(formatearEtiqueta(etiBlqThen));
        resultado.append(blqSi);
        resultado.append(grarCodSaltoInc(etiFin));

        // Caso false
        resultado.append(formatearEtiqueta(etiBlqElse));
        resultado.append(blqSino);
        resultado.append(grarCodSaltoInc(etiFin));

        // Fin if
        resultado.append(formatearEtiqueta(etiFin));

        return resultado.toString();
    }

    protected String procesarMientras(Mientras m, String cond, String blq) {
        StringBuilder resultado = new StringBuilder();

        String etiInicioWhile = getNuevaEtiqueta("inicio_while");
        String etiBucleWhile = getNuevaEtiqueta("bucle_while");
        String etiFinWhile = getNuevaEtiqueta("fin_while");

        resultado.append(formatearEtiqueta(etiInicioWhile));

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        resultado.append(grarCodSaltoCond(cond, etiBucleWhile, etiFinWhile));
        resultado.append(formatearEtiqueta(etiBucleWhile));
        resultado.append(blq);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        resultado.append(grarCodSaltoInc(etiInicioWhile));

        resultado.append(formatearEtiqueta(etiFinWhile));

        return resultado.toString();
    }

    protected String procesarRetorno(Retorno r, String expr) {
        String tipoRetorno = TIPO_IR.get(r.getExpr().getTipo()).fst;
        String resultado = String.format("ret %s %s\n", tipoRetorno, r.getExpr());
        return resultado;
    }

    // *** PROCESOS NO UTILIZADOS ***
    protected String procesarNodo(Nodo n) {
        return null;
    }

    protected String procesarCuando(Cuando cc, String expr, List<String> casosCuando, String blqElse) {
        return null;
    }

    protected String procesarCasoCuando(CasoCuando cc, String expr, String blq) {
        return null;
    }
}