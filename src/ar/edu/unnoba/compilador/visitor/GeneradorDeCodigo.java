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

public class GeneradorDeCodigo extends Visitor {
    // TODO: esta clase hay que revisarla toda, la adapté rápido del ejemplo original para que compile
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)

    private StringBuilder codigo;
    private String nombreArchivoFuente;

    private Map<String, SimboloFuncion> tablaFunciones;

    // *** AUXILIARES ***

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> LLVM_IR_TYPE_INFO = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

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

    private String grarCodDecVar(DecVar dv) {
        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        String valorIR;

        if (dv instanceof DecVarInicializada) {
            // Tomo el valor con la que fue inicializada
            valorIR = String.valueOf(((DecVarInicializada) dv).getExpresion().evaluar());
        } else {
            // No fue inicializada, tomo el valor por defecto
            valorIR = LLVM_IR_TYPE_INFO.get(dv.getTipo()).snd;
        }

        StringBuilder resultado = new StringBuilder();

        // Mostrar comentario con la declaración en el lenguaje original
        resultado.append(String.format("; variable %s is %s = %s\n",
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

    private String grarCodSaltoInc(String etiquetaDestino) {
        return String.format("br label %%%s\n", etiquetaDestino);
    }

    private String grarCodSaltoCond(String cond, String etiquetaTrue, String etiquetaFalse) {
        return String.format("br %s, label %%%s, label %%%s\n", cond, etiquetaTrue, etiquetaFalse);
    }

    public String generarCodigo(Programa p, String nombreArchivoFuente) throws ExcepcionDeAlcance {
        this.nombreArchivoFuente = nombreArchivoFuente;
        tablaFunciones = p.getTablaFunciones();

        super.procesar(p);
        return codigo.toString();
    }

    // *** VISITORS ***

    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
        codigo = new StringBuilder();
        codigo.append(String.format("; Programa: %s\n", p.getNombre()))
              .append(String.format("source_filename = \"%s\"\n", nombreArchivoFuente));

        try {
            codigo.append(getHostTarget());
        } catch (IOException | InterruptedException e) {
            // algo falló... dejarlo hardcodeado
            if (System.getProperty("os.name").startsWith("Windows")) {
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
    public void visit(Bloque b) throws ExcepcionDeAlcance {
        if (b.esProgramaPrincipal()) {
            codigo.append("\ndefine i32 @main(i32, i8**) {\n");
            super.visit(b);
            codigo.append("ret i32 0\n");
            codigo.append("}\n\n");
        } else {
            super.visit(b);
        }
    }

    @Override
    public void visit(Asignacion asig) throws ExcepcionDeAlcance {
        // FIXME: acá no funcionan las conversiones implícitas

        // Primero visito a la expresión para delegarle la generación de las vars. auxs. necesarias
        // TODO ¿cómo sé si ya generé una variable de esta expresión para no hacerlo de nuevo?
        asig.getExpresion().accept(this);

        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();
        String origen = asig.getExpresion().getRefIR();
        String tipoOrigen = LLVM_IR_TYPE_INFO.get(asig.getExpresion().getTipo()).fst;
        String destino = svDestino.getNombreIR();
        String tipoDestino = LLVM_IR_TYPE_INFO.get(svDestino.getTipo()).fst;
        // tipoOrigen y tipoDestino deberían ser iguales, pero lo dejo así para detectar algún error
        // y de paso usar los nombres de las variables para que quede un poco más claro lo que se hace

        codigo.append(String.format("; visit(Asignacion)\n"));
        codigo.append(String.format("store %1$s %2$s, %3$s* %4$s ; %2$s = %4$s\n",
                tipoOrigen, origen, tipoDestino, destino));
    }

    @Override
    public void visit(DecVar dv) {
        grarCodDecVar(dv);
    }

    @Override
    public void visit(DecVarInicializada dvi) {
        grarCodDecVar(dvi);
    }

    @Override
    public void visit(DecFuncion df) throws ExcepcionDeAlcance {
        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo, nombre y parámetros
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(simboloFun.getTipo()).fst;

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < df.getArgs().size(); i++) {
            SimboloVariable sArg = (SimboloVariable) df.getArgs().get(i).getIdent();

            String tipoRetornoArg = LLVM_IR_TYPE_INFO.get(sArg.getTipo()).fst;
            String nombreIR = sArg.getNombreIR();

            String sep = i != df.getArgs().size() - 1 ? ", " : "";
            params.append(String.format("%s %s%s", tipoRetornoArg, nombreIR, sep));
        }

        // Definir la función
        codigo.append(String.format("\ndefine %s @%s(%s) {\n", tipoRetorno, simboloFun.getNombreIR(), params));

        super.visit(df);

        codigo.append("}\n\n");
    }



    @Override
    public void visit(Identificador ident) {
        // Genera el store sobre un refIR para poder acceder a una variable

        // En esta etapa este Identificador va a ser siempre un SimboloVariable. Tengo que utilizarlo así porque
        // si creo visit(SimboloVariable) da muchos problemas (por ej. se rompe el graficado del AST)
        SimboloVariable sv = (SimboloVariable) ident;

        String nombreIR = sv.getNombreIR();
        String tipoIR = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        String refIR = Normalizador.getNvoNomVarAux("sv");
        sv.setRefIR(refIR);

        codigo.append(String.format("; visit(SimboloVariable %s)\n", sv.getNombre())); // borrar desp. es para ver si hace cagadas
        codigo.append(String.format("%1$s = load %2$s, %2$s* %3$s\n", refIR, tipoIR, nombreIR));
    }

    @Override
    public void visit(Literal lit) throws ExcepcionDeAlcance {
        // Este visitor genere una variable auxiliar para utilizar los valores literales
        // Como alternativa a generar la variable, podríamos guardar el valor en refIR
        // pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.

        // TODO ver acá el tema ese de truncar los valores

        String nombreIR;
        String refIR;

        Tipo tipoParser = lit.getTipo();
        String tipoIR = LLVM_IR_TYPE_INFO.get(tipoParser).fst;
        String valorParser = lit.getValor();
        String valorIR;

        if (tipoParser == Tipo.INTEGER) {
            nombreIR = Normalizador.getNvoNomVarAux("lit.i");
            refIR = Normalizador.getNvoNomVarAux("ref");
            valorIR = valorParser;
        } else if (tipoParser == Tipo.FLOAT) {
            nombreIR = Normalizador.getNvoNomVarAux("lit.f");
            refIR = Normalizador.getNvoNomVarAux("ref");
            valorIR = String.valueOf(valorParser);
        } else if (tipoParser == Tipo.BOOLEAN) {
            nombreIR = Normalizador.getNvoNomVarAux("lit.b");
            refIR = Normalizador.getNvoNomVarAux("ref");
            valorIR = valorParser == "false" ? "0" : "1";
        } else {
            throw new ExcepcionDeAlcance("Valor de tipo inesperado: " + lit.getTipo());
        }

        lit.setRefIR(refIR);

        codigo.append("; visit(Literal)\n")
              .append(String.format("%s = alloca %s\n", nombreIR, tipoIR))
              .append(String.format("store %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR))
              .append(String.format("%1$s = load %2$s, %2$s* %3$s\n", refIR, tipoIR, nombreIR));
    }

    // *** PROCESOS ***
    // TODO: Sacar

    protected void procesarOperacionBinaria(OperacionBinaria ob, String decAuxIzq, String decAuxDer) {
        ob.setRefIR(Normalizador.getNvoNomVarAux("ob"));
        String tipoLLVM = LLVM_IR_TYPE_INFO.get(ob.getIzquierda().getTipo()).fst;

        // En decAuxIzq y decAuxDer vienen las declaraciones de variables auxiliares que voy a necesitar
        codigo.append(decAuxIzq);
        codigo.append(decAuxDer);

        if (ob instanceof OperacionBinariaAritmetica) {
            codigo.append(String.format("%1$s = %2$s %3$s %4$s, %5$s ; %1$s = %4$s %6$s %5$s\n",
                    ob.getRefIR(), ob.getInstruccionIR(), tipoLLVM, ob.getIzquierda().getRefIR(),
                    ob.getDerecha().getRefIR(), ob.getNombre()));
        } else if (ob instanceof OperacionBinariaLogica) {
            codigo.append("; procesarOperacionBinaria -> OpBinLog sin implementar");
        } else if (ob instanceof Relacion) {
            codigo.append("; procesarOperacionBinaria -> Relacion sin implementar");
        }
    }

    protected void procesarInvocacionFuncion(InvocacionFuncion invoFun) {
        // TODO
        codigo.append(String.format("; Invocación a %s()\n", invoFun.getNombre()));
    }

    protected void procesarSiEntonces(String cond, String blqSi) {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiFin = getNuevaEtiqueta("fin_if");

        // Salto condicional
        codigo.append(grarCodSaltoCond(cond, etiBlqThen, etiFin));

        // Caso true
        codigo.append(formatearEtiqueta(etiBlqThen));
        codigo.append(blqSi);
        codigo.append(grarCodSaltoInc(etiFin));

        // Fin if
        codigo.append(formatearEtiqueta(etiFin));
    }

    protected void procesarSiEntoncesSino(String cond, String blqSi, String blqSino) {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiBlqElse = getNuevaEtiqueta("blq_else");
        String etiFin = getNuevaEtiqueta("fin_if");

        // Salto condicional
        codigo.append(grarCodSaltoCond(cond, etiBlqThen, etiBlqElse));

        // Caso true
        codigo.append(formatearEtiqueta(etiBlqThen));
        codigo.append(blqSi);
        codigo.append(grarCodSaltoInc(etiFin));

        // Caso false
        codigo.append(formatearEtiqueta(etiBlqElse));
        codigo.append(blqSino);
        codigo.append(grarCodSaltoInc(etiFin));

        // Fin if
        codigo.append(formatearEtiqueta(etiFin));
    }

    protected void procesarMientras(Mientras m, String cond, String blq) {
        String etiInicioWhile = getNuevaEtiqueta("inicio_while");
        String etiBucleWhile = getNuevaEtiqueta("bucle_while");
        String etiFinWhile = getNuevaEtiqueta("fin_while");

        codigo.append(formatearEtiqueta(etiInicioWhile));

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        codigo.append(grarCodSaltoCond(cond, etiBucleWhile, etiFinWhile));
        codigo.append(formatearEtiqueta(etiBucleWhile));
        codigo.append(blq);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        codigo.append(grarCodSaltoInc(etiInicioWhile));

        codigo.append(formatearEtiqueta(etiFinWhile));
    }

    protected void procesarRetorno(Retorno r, String expr) {
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(r.getExpr().getTipo()).fst;
        codigo.append(String.format("ret %s %s\n", tipoRetorno, r.getExpr()));
    }
}