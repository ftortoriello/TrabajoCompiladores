package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
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

    private String nombreArchivo;

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

    public String procesar(Programa p, String n) throws ExcepcionDeAlcance {
        nombreArchivo = n;
        tablaFunciones = p.getTablaFunciones();

        String resultado = visit(p);
        return resultado;
    }

    // *** VISITORS ***

    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(String.format(";Programa: %s\n", p.getNombre()));
        // FIXME: Iría entrada.txt acá?
        resultado.append(String.format("source_filename = \"%s\"\n", nombreArchivo));

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

    // En el ejemplo tiene un nodo específico para los Write (nosotros no), pero vamos a tener que hacer algo parecido
    // si una invocación es de las write
    /*
    @Override
    public String visit(Write w) throws ExcepcionDeCompilacion {
        StringBuilder resultado = new StringBuilder();
        resultado.append(w.getExpresion().accept(this));
        String tipo_llvm = this.LLVM_IR_TYPE_INFO.get(w.getExpresion().getTipo()).getKey();
        String variable_print = "@.integer";
        String ref_to_print = w.getExpresion().getIr_ref();
        if (w.getExpresion().getTipo() == Tipo.FLOAT){
            String temp_ref_to_print = this.newTempId();
            resultado.append(String.format("%1$s = fpext float %2$s to double\n", temp_ref_to_print, ref_to_print));
            ref_to_print = temp_ref_to_print;
            tipo_llvm = "double";
            variable_print = "@.float";
        }
        resultado.append(String.format("%1$s = call i32 (i8*, ...) @printf(i8* getelementptr([4 x i8], [4 x i8]* %2$s, i32 0, i32 0), %3$s %4$s)\n",
                this.newTempId(), variable_print, tipo_llvm, ref_to_print));
        return resultado.toString();
    }
     */

    // TODO print

    @Override
    public String visit(DecVar dv) {
        return grarCodDecVar(dv);
    }

    @Override
    public String visit(DecVarInicializada dvi) {
        return grarCodDecVar(dvi);
    }

    /*
    @Override
    public String visit(Identificador ident) {
        // La idea de este visitor es detectar cuando se usa una variable global
        // para guardar el valor en una variable auxiliar mediante load.
        // TODO como sé si ya generé esto para no hacerlo de nuevo?

        SimboloVariable sv = (SimboloVariable) ident;

        String tipoLLVM = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        // El nombre original, pero normalizado y único
        String nombreIR = sv.getNombreIR();
        // El nombre de la var. aux. desde la cual puedo leer el valor
        String auxIR = sv.getAuxIR();

        String resultado = String.format("%1$s = load %2$s, %2$s* %3$s ; %1$s = %3$s\n", auxIR, tipoLLVM, nombreIR);

        return resultado;
    }
    */

    public String visit(Asignacion asig) {

        SimboloVariable sv = (SimboloVariable) asig.getIdent();
        String nombreIR = sv.getNombreIR();
        // TODO asignarle refIR a las expresiones
        String refIR = asig.getExpresion().getRefIR();

        StringBuilder resultado = new StringBuilder();

        resultado.append(String.format("; %s = %s\n", nombreIR, refIR));
        return resultado.toString();

        /*

        // FIXME: acá no funcionan las conversiones implícitas
        SimboloVariable sv = (SimboloVariable) asig.getIdent();
        String tipoLLVM = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;

        StringBuilder resultado = new StringBuilder();

        resultado.append(String.format("store %1$s %2$s, %1$s* %3$s\t; %4$s = %2$s\n",
                tipoLLVM, sv.getRefIR(), sv.getNombreIR(), sv.getNombre()));

        return resultado.toString();
        */
    }

    @Override
    public String visit(Literal l) {
        String val;

        if (l.getTipo() == Tipo.FLOAT) {
            val = Double.toString(Float.valueOf(l.getValor()));
        } else {
            val = l.getValor();
        }
        // TODO boolean

        l.setRefIR(val);
        return "";
    }

    // *** PROCESOS ***

    @Override
    protected String procesarPrograma(Programa p, String enc, String cpo) {
        return enc + cpo;
    }

    @Override
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

    @Override
    protected String procesarDecFuncion(DecFuncion df, List<String> args, String cuerpo) {

        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());
        if (simboloFun == null) {
            throw new RuntimeException("No se encontró en la tabla a la función «" + df.getNombre() +
                "» cuando ya debería estar definida (¿está mal definido el alcance?)");
        }

        // Elementos que necesito para definir la función: tipo, nombre y parámetros
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(simboloFun.getTipo()).fst;

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < df.getArgs().size(); i++) {
            SimboloVariable sArg = (SimboloVariable) df.getArgs().get(i).getIdent();

            String tipoRetornoArg = LLVM_IR_TYPE_INFO.get(sArg.getTipo()).fst;
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

    @Override
    protected String procesarAsignacion(Asignacion asig, String decVarAux, String expr) {
        return null;
    }

    @Override
    protected String procesarInvocacionFuncion(InvocacionFuncion invoFun) {
        // TODO
        return String.format("; Invocación a %s()\n", invoFun.getNombre());
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        // TODO: ver si dejamos visits o procesar separados
        return "OP_BIN (SIN IMPLEMENTAR)";
        /*
        StringBuilder resultado = new StringBuilder();
        // resultado.append(super.visit(ob));
        ob.setRefIR(this.getNombreVarAux());
        String tipoLLVM = this.LLVM_IR_TYPE_INFO.get(ob.getIzquierda().getTipo()).fst;
        resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s\n", ob.getRefIR(),
                ob.getInstruccionIR(), tipoLLVM, ob.getIzquierda().getRefIR(), ob.getDerecha().getRefIR()));
        return resultado.toString();
        */
    }

    @Override
    protected String procesarRetorno(Retorno r, String expr) {
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(r.getExpr().getTipo()).fst;
        String resultado = String.format("ret %s %s\n", tipoRetorno, r.getExpr());
        return resultado;
    }

    @Override
    protected String procesarNodo(Nodo n) {
        // Esto tendría que devolver el nombreIR?
        return n.getNombre();
    }

    @Override
    protected String procesarDecVar(DecVar dv, String ident) {
        return null;
    }

    @Override
    protected String procesarDecVarInicializada(DecVarInicializada dvi, String ident, String expr) {
        return null;
    }

    // Ya no tenemos when pero hay que definir sí o sí esto
    @Override
    protected String procesarCuando(Cuando cc, String expr, List<String> casosCuando, String blqElse) { return null; }
    @Override
    protected String procesarCasoCuando(CasoCuando cc, String expr, String blq) { return null; }
}
