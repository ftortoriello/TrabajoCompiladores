package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeCompilacion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import jflex.base.Pair;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorDeCodigo extends Visitor<String> {

    // TODO: esta clase hay que revisarla toda, la adapté rápido del ejemplo original para que compile
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)

    private String nombreArchivo;
    private Alcance alcanceGlobal;
    private Alcance alcanceActual;

    private Map<String, SimboloFuncion> tablaFunciones;

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> LLVM_IR_TYPE_INFO = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    public String normalizar(String cadena) {
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    // Genera un nombre normalizado y único (seguro?) para una nueva variable global
    // TODO: asignarles un nro. a las funciones/variables sólo si es necesario (o sea si chocan con otra)
    public String getNombreVarGlobal(DecVar dv) {
        String nombreNormalizado = normalizar(dv.getIdent().getNombre());
        return String.format("@g_%s_%s", nombreNormalizado, getID());
    }

    // Genera un nombre normalizado y único para una nueva variable local
    public String getNombreVarLocal(DecVar dv) {
        String nombreNormalizado = normalizar(dv.getIdent().getNombre());
        return String.format("%%%s_%s", nombreNormalizado, getID());
    }

    public String getNombreVarAux() {
        return String.format("%%$t_%s", getID());
    }

    // Devuelve un nombre único para una nueva etiqueta
    public String getEtiqueta(String nombre) {
        return String.format("e_%s_%s", nombre, getID());
    }

    private String generarCodigoVarGlobal(String nombreIR, String tipoIR, String valorIR) {
        return String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR);
    }

    private String generarCodigoVarLocal(String nombreIR, String tipoIR, String valorIR) {
        StringBuilder codigoIR = new StringBuilder();

        codigoIR.append(String.format("%s = alloca %s\n", nombreIR, tipoIR));
        codigoIR.append(String.format("store %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        codigoIR.append(String.format("%3$s = load %2$s, %2$s* %1$s\n", codigoIR, tipoIR, getNombreVarAux()));

        return codigoIR.toString();
    }

    private String generarCodigoDecVar(DecVar dv) {
        String codigoIR;
        String nombreIR;
        String tipoIR = LLVM_IR_TYPE_INFO.get(dv.getTipo()).fst;
        String valorIR;

        if (dv instanceof DecVarInicializada) {
            // Tomo el valor con la que fue inicializada
            valorIR = String.valueOf(((DecVarInicializada) dv).getExpresion().evaluar());
        } else {
            // No fue inicializada, tomo el valor por defecto
            valorIR =  LLVM_IR_TYPE_INFO.get(dv.getTipo()).snd;
        }

        if (alcanceActual.getPadre() == null) {
            // Es variable global
            nombreIR = getNombreVarGlobal(dv);
            codigoIR = generarCodigoVarGlobal(nombreIR, tipoIR, valorIR);
        } else {
            // Es variable local
            nombreIR = getNombreVarLocal(dv);
            codigoIR = generarCodigoVarLocal(nombreIR, tipoIR, valorIR);
        }

        // Actualizo el nombreIR en el identificador y en la tabla de símbolos
        // TODO: ver de usar una cosa o la otra
        dv.getIdent().setNombreIR(nombreIR);
        String nombreVar = dv.getIdent().getNombre();
        SimboloVariable sv = alcanceActual.get(nombreVar);
        //sv.setNombreIR(nombreIR);

        return codigoIR;
    }

    public String procesar(Programa programa, String nombre) throws ExcepcionDeAlcance {
        nombreArchivo = nombre;
        alcanceGlobal = alcanceActual = programa.getAlcance();
        tablaFunciones = programa.getTablaFunciones();
        return visit(programa);
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
    public String visit(Bloque b) throws ExcepcionDeAlcance {
        alcanceActual = b.getAlcance();
        super.visit(b);
        alcanceActual = alcanceActual.getPadre();
        return null;
    }

    @Override
    public String visit(DecFuncion df) throws ExcepcionDeAlcance {
        alcanceActual = df.getAlcance();
        StringBuilder resultado = new StringBuilder();

        // Parámetros que necesito para definir la función
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(df.getTipo()).fst;
        String nombreFuncion = String.format("%s_%s", normalizar(df.getNombre()), getID());

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < df.getArgs().size(); i++) {
            DecVar arg = df.getArgs().get(i);
            String tipoRetornoArg = LLVM_IR_TYPE_INFO.get(arg.getTipo()).fst;
            String nombreIR = getNombreVarLocal(arg);
            arg.getIdent().setNombreIR(nombreIR);
            String sep = i != df.getArgs().size() - 1 ? ", " : "";
            params.append(String.format("%s %s%s", tipoRetornoArg, nombreIR, sep));
        }

        resultado.append(String.format("\ndeclare %s @%s(%s) {\n", tipoRetorno, nombreFuncion, params));

        // Visito solo el bloque porque los argumentos los proceso acá
        super.visit(df.getBloque());

        resultado.append("\n}\n\n");
        alcanceActual = alcanceActual.getPadre();
        return resultado.toString();
    }

    @Override
    public String visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        return generarCodigoDecVar(dvi);
    }

    @Override
    public String visit(DecVar dv) throws ExcepcionDeAlcance {
        return generarCodigoDecVar(dv);
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

    @Override
    public String visit(SiEntonces se) throws ExcepcionDeAlcance {
        return ";If no implementado";
    }

    @Override
    public String visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        return ";If no implementado";
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
        ob.setRefIR(this.getNombreVarAux());
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

    // *** PROCESOS ***

    // Base

    @Override
    protected String procesarPrograma(Programa p, String enc, String cpo) {
        return enc + cpo;
    }

    @Override
    protected String procesarEncabezado(Encabezado encabezado, List<String> sentencias) {
        // Retorno solo la lista de sentencias, el encabezado en el visitor de esta clase
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach(resultado::append);
        return resultado.toString();
    }

    @Override
    protected String procesarBloque(Bloque bloque, List<String> sentencias) {
        return this.procesarBloque(bloque, new ArrayList<>(), sentencias);
    }

    @Override
    protected String procesarBloque(Bloque bloque, List<String> declaraciones, List<String> sentencias) {

        return ";Proceso de bloque sin implementar";

        /*

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

        return resultado.toString();
        */
    }

    @Override
    protected String procesarNodo(Nodo n) {
        return null;
    }

    // Declaraciones

    @Override
    protected String procesarDecFuncion(List<String> args, String cuerpo) {
        return cuerpo;
    }

    @Override
    protected String procesarVarInicializada(String ident, String expr) {
        return ident + expr;
    }

    // Asignación

    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) {
        return null;
    }

    // Seleccion/iteración

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

    // Operaciones

    @Override
    protected String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        return ei + ed;
    }

    // Control

    @Override
    protected String procesarRetorno(Retorno r, String expr) {
        return null;
    }
}
