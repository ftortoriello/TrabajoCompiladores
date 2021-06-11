package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeCompilacion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import jflex.base.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorDeCodigo extends Visitor<String> {

    // TODO: esta clase hay que revisarla toda, la adapté rápido del ejemplo original para que compile
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)

    private String nombreArchivo;
    private Alcance alcanceActual;

    private void setAlcanceActual(Alcance alcanceActual) {
        this.alcanceActual = alcanceActual;
    }

    // Map para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> LLVM_IR_TYPE_INFO = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    // Genera un nombre único (seguro?) para una nueva variable global
    public String getNombreVarGlobal(DecVar dv) {
        return String.format("@g_%s_%s", dv.getIdent().getNombre(), getID());
    }

    // Genera un nombre único para una nueva variable local
    public String getNombreVarLocal(DecVar dv) {
        return String.format("%%s_%s", dv.getIdent().getNombre(), getID());
    }

    // Devuelve un nombre único para un ID temporal (?)
    public String newIdTemp() {
        return String.format("%%t$%s", getID());
    }

    // Devuelve un nombre único para una nueva etiqueta
    public String newEtiqueta(String nombre) {
        return String.format("eti_%s_%s", nombre, getID());
    }

    public String procesar(Programa programa, String nombre) throws ExcepcionDeAlcance {
        this.nombreArchivo = nombre;
        this.alcanceActual = programa.getAlcance();
        return this.visit(programa);
    }

    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(String.format(";Programa: %s\n", p.getNombre()));
        resultado.append(String.format("source_filename = \"%s\"\n", nombreArchivo));

        // TODO: podríamos tomar el target de un archivo que no esté en el git asi no nos pisamos a cada rato
        // Target Bruno:
        resultado.append("target datalayout = \"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n");
        resultado.append("target triple = \"x86_64-pc-windows-msvc19.28.29335\"\n");

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
    public String visit(DecFuncion df) throws ExcepcionDeAlcance {
        // TODO
        alcanceActual = df.getAlcance();
        super.visit(df);
        alcanceActual = alcanceActual.getPadre();
        return null;
    }

    @Override
    protected String procesarEncabezado(Encabezado encabezado, List<String> sentencias) {
        return null;
    }

    @Override
    protected String procesarBloque(Bloque bloque, List<String> sentencias) {
        return this.procesarBloque(bloque, new ArrayList<>(), sentencias);
    }

    @Override
    protected String procesarBloque(Bloque bloque, List<String> declaraciones, List<String> sentencias) {

        alcanceActual = bloque.getAlcance();

        StringBuilder resultado = new StringBuilder();
        if (bloque.esProgramaPrincipal()) {
            declaraciones.forEach((declaracion) -> {
                resultado.append(declaracion);
            });
            resultado.append("define i32 @main(i32, i8**) {\n");
            sentencias.forEach((sentencia) -> {
                resultado.append(sentencia);
            });
            resultado.append("ret i32 0\n");
            resultado.append("}");

        } else {
            sentencias.forEach((sentencia) -> {
                resultado.append(sentencia);
            });
        }

        alcanceActual = bloque.getAlcance().getPadre();
        return resultado.toString();
    }

    @Override
    public String visit(DecVar dv) {
        // TODO extender por si es DecVarInicializada
        // TODO normalizar
        StringBuilder resultado = new StringBuilder();
        String tipoIR = LLVM_IR_TYPE_INFO.get(dv.getTipo()).fst;
        String valorIR = LLVM_IR_TYPE_INFO.get(dv.getTipo()).snd;
        String nombreIR;

        if (alcanceActual.getPadre() == null) {
            nombreIR = getNombreVarGlobal(dv);
            resultado.append(String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            // TODO declaración variable local
            nombreIR = getNombreVarLocal(dv);
            resultado.append(String.format("NO IMPLEMENTADO\n", nombreIR, tipoIR, valorIR)); // %temp = load i32, i32* @x3
            resultado.append(String.format("NO IMPLEMENTADO\n", nombreIR, tipoIR, valorIR)); // store i32 temp, i32* @x3
        }

        // TODO: lo modifiqué para que modifique el símbolo en vez de la declaración, revisar
        SimboloVariable sv = alcanceActual.resolver(dv.getIdent().getNombre());
        sv.setNombreIR(nombreIR);

        return resultado.toString();
    }

    @Override
    public String visit(Asignacion a) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(a.getExpresion().accept(this));
        SimboloVariable sv = this.alcanceActual.resolver(a.getIdent().getNombre());
        String tipoLLVM = this.LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        resultado.append(String.format("store %1$s %2$s, %1$s* %3$s ; %3$s = %2$s\n",
                tipoLLVM, a.getExpresion().getRefIR(), sv.getNombreIR(), sv.getNombre()));
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

    public String generarCodigoOperacionBinaria(OperacionBinaria ob) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(super.visit(ob));
        ob.setRefIR(this.newIdTemp());
        String tipoLLVM = this.LLVM_IR_TYPE_INFO.get(ob.getIzquierda().getTipo()).fst;
        resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s\n", ob.getRefIR(),
                ob.getInstruccionIR(), tipoLLVM, ob.getIzquierda().getRefIR(), ob.getDerecha().getRefIR()));
        return resultado.toString();
    }

    @Override
    public String visit(Literal l) {
        // TODO ver si se guardan bien flotantes y enteros
        l.setRefIR(String.valueOf(l.getValor()));
        return null;
    }

    @Override
    public String visit(OperacionBinaria s) throws ExcepcionDeAlcance {
        return this.generarCodigoOperacionBinaria(s);
    }

    // En nuestro caso no tenemos más identificadores en esta etapa (adaptar para símbolos?)
    /*
    @Override
    public String visit(Identificador i) throws ExcepcionDeCompilacion{
        StringBuilder resultado = new StringBuilder();
        Object res = this.alcance_actual.resolver(i.getNombre());
        String temp_id = this.newTempId();
        if (res instanceof DeclaracionVariable){
            DeclaracionVariable dv = ((DeclaracionVariable) res);
            String tipo_llvm = this.LLVM_IR_TYPE_INFO.get(dv.getTipo()).getKey();
            String llvm_ref = dv.getIr_name();
            resultado.append(String.format("%1$s = load %2$s, %2$s* %3$s ; %1$s = %4$s\n",
                    temp_id, tipo_llvm, llvm_ref, i.getNombre()));
            i.setIr_ref(temp_id);
            return resultado.toString();
        }
        throw new ExcepcionDeCompilacion("No se encontró el identificador "+i.getNombre());
    }
    */

    @Override
    protected String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        return ei + ed;
    }

    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) {
        return null;
    }

    // Métodos heredados que capaz no haga falta implementar (que quilombo...)

    @Override
    protected String procesarNodo(Nodo n) {
        return null;
    }

    @Override
    protected String procesarPrograma(Programa p, String enc, String blq) {
        return null;
    }

    @Override
    protected String procesarVarInicializada(String ident, String expr) {
        return null;
    }

    @Override
    protected String procesarDecFuncion(List<String> args, String cuerpo) {
        return null;
    }

    @Override
    protected String procesarSiEntonces(String cond, String blq) {
        return null;
    }

    @Override
    protected String procesarSiEntoncesSino(String cond, String blqSi, String blqSino) {
        return null;
    }

    @Override
    protected String procesarCuando(Cuando cc, String expr, List<String> casosCuando, String blqElse) {
        return null;
    }

    @Override
    protected String procesarCasoCuando(CasoCuando cc, String expr, String blq) {
        return null;
    }

    @Override
    protected String procesarMientras(Mientras m, String expr, String blq) {
        return null;
    }

    @Override
    protected String procesarRetorno(Retorno r, String expr) {
        return null;
    }
}
