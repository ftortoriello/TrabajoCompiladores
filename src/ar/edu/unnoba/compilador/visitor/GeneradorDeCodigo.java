package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
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

    private Map<String, SimboloFuncion> tablaFunciones;

    private static final Normalizador norm = new Normalizador();

    // *** AUXILIARES ***

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> LLVM_IR_TYPE_INFO = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    // Genera un nombre único para una nueva etiqueta
    public String getEtiqueta(String nombre) {
        return String.format("e_%s_%s", nombre, getID());
    }

    // Genera la declaración en IR para una var. global
    private String generarCodigoVarGlobal(String nombreIR, String tipoIR, String valorIR) {
        return String.format("%s = global %s %s\n", nombreIR, tipoIR, valorIR);
    }

    // Genera la declaración en IR para una var. local
    private String generarCodigoVarLocal(String nombreIR, String tipoIR, String valorIR) {
        StringBuilder codigoIR = new StringBuilder();

        codigoIR.append(String.format("%s = alloca %s\n", nombreIR, tipoIR));
        codigoIR.append(String.format("store %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        codigoIR.append(String.format("%3$s = load %2$s, %2$s* %1$s\n", nombreIR, tipoIR, norm.getNuevoNomVarAux()));

        return codigoIR.toString();
    }

    private String generarCodigoDecVar(DecVar dv) {
        SimboloVariable sv = alcanceActual.resolver(dv.getIdent().getNombre());

        if (sv == null) {
            throw new RuntimeException("No se encontró en la tabla a la variable «" + dv.getIdent().getNombre() +
                    "» cuando ya debería estar definida (¿está mal definido el alcance?)");
        }

        String nombreIR = sv.getNombreIR();
        String tipoIR = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        String valorIR;

        String codigoIR;

        if (dv instanceof DecVarInicializada) {
            // Tomo el valor con la que fue inicializada
            valorIR = String.valueOf(((DecVarInicializada) dv).getExpresion().evaluar());
        } else {
            // No fue inicializada, tomo el valor por defecto
            valorIR = LLVM_IR_TYPE_INFO.get(dv.getTipo()).snd;
        }

        if (alcanceActual.getPadre() == null) {
            // Es variable global
            codigoIR = generarCodigoVarGlobal(nombreIR, tipoIR, valorIR);
        } else {
            // Es variable local
            codigoIR = generarCodigoVarLocal(nombreIR, tipoIR, valorIR);
        }

        return codigoIR;
    }

    private String generarCodigoSaltoInc(String etiquetaDestino) {
        return String.format("br label %%%s\n", etiquetaDestino);
    }

    private String generarCodigoSaltoCond(String cond, String etiquetaThen, String etiquetaElse) {
        return String.format("br %s, label %%%s, label %%%s\n", cond, etiquetaThen, etiquetaElse);
    }

    public String procesar(Programa p, String n) throws ExcepcionDeAlcance {
        nombreArchivo = n;
        alcanceActual = p.getAlcance();
        tablaFunciones = p.getTablaFunciones();

        String resultado = visit(p);

        alcanceActual = null;
        return resultado;
    }

    // *** VISITORS ***

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
        // Cambio el alcance al de la función para que encuentre los parámetros
        alcanceActual = df.getAlcance();
        String resultado = super.visit(df);
        alcanceActual = df.getAlcance().getPadre();
        return resultado;
    }

    @Override
    public String visit(Bloque blq) throws ExcepcionDeAlcance {
        alcanceActual = blq.getAlcance();
        String resultado = super.visit(blq);
        alcanceActual = blq.getAlcance().getPadre();
        return resultado;
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
            resultado.append("\n}\n\n");
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

        // Parámetros que necesito para definir la función: tipo, nombre y parámetros
        String tipoRetorno = LLVM_IR_TYPE_INFO.get(simboloFun.getTipo()).fst;

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < df.getArgs().size(); i++) {
            String nombreVar = df.getArgs().get(i).getIdent().getNombre();
            SimboloVariable sArg = alcanceActual.resolver(nombreVar);

            String tipoRetornoArg = LLVM_IR_TYPE_INFO.get(sArg.getTipo()).fst;
            String nombreIR = sArg.getNombreIR();

            String sep = i != df.getArgs().size() - 1 ? ", " : "";
            params.append(String.format("%s %s%s", tipoRetornoArg, nombreIR, sep));
        }

        // Declaro la función
        StringBuilder declaracionFunIR = new StringBuilder();
        declaracionFunIR.append(String.format("\ndeclare %s @%s(%s) {\n", tipoRetorno, simboloFun.getNombreIR(), params));
        declaracionFunIR.append(cuerpo);
        declaracionFunIR.append("}\n\n");

        return declaracionFunIR.toString();
    }

    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        resultado.append(a.getExpresion().accept(this));

        SimboloVariable sv = alcanceActual.resolver(a.getIdent().getNombre());
        String tipoLLVM = LLVM_IR_TYPE_INFO.get(sv.getTipo()).fst;
        resultado.append(String.format("store %1$s %2$s, %1$s* %3$s ; %3$s = %2$s\n",
                tipoLLVM, a.getExpresion().getRefIR(), sv.getNombreIR()));

        return resultado.toString();
    }

    @Override
    protected String procesarInvocacionFuncion(InvocacionFuncion invoFun) {
        // TODO
        return String.format("; Invocación a %s()\n", invoFun.getNombre());
    }

    @Override
    protected String procesarSiEntonces(String cond, String blqSi) {
        StringBuilder resultado = new StringBuilder();

        String etiBlqThen = getEtiqueta("blq_then");
        String etiFin = getEtiqueta("fin_if");

        // Salto condicional
        resultado.append(generarCodigoSaltoCond(cond, etiBlqThen, etiFin));

        // Caso true
        resultado.append(etiBlqThen);
        resultado.append(blqSi);
        resultado.append(generarCodigoSaltoInc(etiFin));

        // Fin if
        resultado.append(etiFin);

        return resultado.toString();
    }

    @Override
    protected String procesarSiEntoncesSino(String cond, String blqSi, String blqSino) {
        StringBuilder resultado = new StringBuilder();

        String etiBlqThen = getEtiqueta("blq_then");
        String etiBlqElse = getEtiqueta("blq_else");
        String etiFin = getEtiqueta("fin_if");

        // Salto condicional
        resultado.append("\n; Comienzo del if, evaluar cond.\n");
        resultado.append(generarCodigoSaltoCond(cond, etiBlqThen, etiBlqElse));

        // Caso true
        resultado.append(etiBlqThen + ":\n");
        resultado.append("; Bloque then\n");
        resultado.append(blqSi);
        resultado.append(generarCodigoSaltoInc(etiFin));

        // Caso false
        resultado.append(etiBlqElse + ":\n");
        resultado.append("; Bloque else\n");
        resultado.append(blqSino);
        resultado.append(generarCodigoSaltoInc(etiFin));

        // Fin if
        resultado.append(etiFin + ":\n");
        resultado.append("\n; Fin del if\n\n");

        return resultado.toString();
    }

    @Override
    protected String procesarMientras(Mientras m, String expr, String blq) {
        return "; While no implementado\n";
    }

    @Override
    public String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
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
        return "; Sentencias de control no implementadas\n";
    }

    @Override
    protected String procesarNodo(Nodo n) {
        // Esto tendría que devolver el nombreIR?
        return n.getNombre();
    }

    @Override
    protected String procesarDecVar(DecVar dv, String ident) {
        return generarCodigoDecVar(dv);
    }

    @Override
    protected String procesarDecVarInicializada(DecVarInicializada dvi, String ident, String expr) {
        return generarCodigoDecVar(dvi);
    }

    // Ya no tenemos when pero hay que definir sí o sí esto
    @Override
    protected String procesarCuando(Cuando cc, String expr, List<String> casosCuando, String blqElse) { return null; }
    @Override
    protected String procesarCasoCuando(CasoCuando cc, String expr, String blq) { return null; }
}
