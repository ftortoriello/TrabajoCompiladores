package ar.edu.unnoba.compilador.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
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

/* Visitor para generar el alcance global y construir la tabla de símbolos
 * globales.
 */
public class GeneradorDeAlcanceGlobal extends Visitor<Void> {
    private Alcance alcanceGlobal;
    private Map<String, SimboloFuncion> tablaFunciones;

    // Agregar el símbolo de la variable al ámbito global
    private void agregarSimboloVarGlobal(DecVar d) throws ExcepcionDeAlcance {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (alcanceGlobal.containsKey(nombre)) {
            throw new ExcepcionDeAlcance(
                    String.format("La variable global «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            alcanceGlobal.get(nombre).getTipo()));
        }

        // Defino de antemano los nombres que necesitamos para el IR
        String nombreIR = Normalizador.getNvoNomVarGbl(nombre);
        Boolean esGlobal = true;

        SimboloVariable simbolo = new SimboloVariable(d, nombreIR, esGlobal);

        alcanceGlobal.put(nombre, simbolo);
    }

    private void agregarSimboloFuncion(DecFuncion d) throws ExcepcionDeAlcance {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (tablaFunciones.containsKey(nombre)) {
            throw new ExcepcionDeAlcance(
                    String.format("La función «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(), tablaFunciones.get(nombre).getTipo()));
        }

        String nombreIR = Normalizador.getNvoNomFun(nombre);
        SimboloFuncion simbolo = new SimboloFuncion(d, nombreIR);
        tablaFunciones.put(nombre, simbolo);
    }

    @Override
    public Void visit(Programa p) throws ExcepcionDeAlcance {
        alcanceGlobal = new Alcance("global");
        p.setAlcance(alcanceGlobal);

        tablaFunciones = new HashMap<>();
        p.setTablaFunciones(tablaFunciones);

        return super.visit(p);
    }

    // No procesar los bloques en este Visitor
    @Override
    public Void visit(Bloque b) {
        return null;
    }

    @Override
    public Void visit(DecFuncion df) throws ExcepcionDeAlcance {
        // Agregar a la tabla la declaración de la función
        agregarSimboloFuncion(df);
        // No visitar los parámetros ni el cuerpo
        return null;
    }

    // Visit de declaraciones de variables globales (en el encabezado).
    @Override
    public Void visit(DecVar dv) throws ExcepcionDeAlcance {
        agregarSimboloVarGlobal(dv);
        return super.visit(dv);
    }

    @Override
    public Void visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        if (dvi.getExpresion() instanceof InvocacionFuncion) {
            throw new ExcepcionDeAlcance(String.format(
                    "«%s»: No se puede invocar a una función desde la inicialización de una variable global.",
                    dvi.getIdent()));
        }
        agregarSimboloVarGlobal(dvi);
        return super.visit(dvi);
    }


    @Override
    protected Void procesarPrograma(Programa p, Void enc, Void blq) {
        return null;
    }

    @Override
    protected Void procesarNodo(Nodo n) {
        return null;
    }

    @Override
    protected Void procesarEncabezado(Encabezado e, List<Void> declaraciones) {
        return null;
    }

    @Override
    protected Void procesarBloque(Bloque b, List<Void> sentencias) {
        return null;
    }

    @Override
    protected Void procesarOperacionBinaria(OperacionBinaria ob, Void ei, Void ed) {
        return null;
    }

    @Override
    protected Void procesarDecVar(DecVar dv, Void ident) {
        return null;
    }

    @Override
    protected Void procesarDecVarInicializada(DecVarInicializada dvi, Void ident, Void expr) {
        return null;
    }

    @Override
    protected Void procesarAsignacion(Asignacion a, Void identificador, Void expresion) {
        return null;
    }

    @Override
    protected Void procesarInvocacionFuncion(InvocacionFuncion invoFun) {
        return null;
    }

    @Override
    protected Void procesarDecFuncion(DecFuncion df, List<Void> args, Void cuerpo) {
        return null;
    }

    @Override
    protected Void procesarSiEntonces(Void cond, Void blq) {
        return null;
    }

    @Override
    protected Void procesarSiEntoncesSino(Void cond, Void blqSi, Void blqSino) {
        return null;
    }

    @Override
    protected Void procesarCuando(Cuando cc, Void expr, List<Void> casosCuando, Void blqElse) {
        return null;
    }

    @Override
    protected Void procesarCasoCuando(CasoCuando cc, Void expr, Void blq) {
        return null;
    }

    @Override
    protected Void procesarMientras(Mientras m, Void expr, Void blq) {
        return null;
    }

    @Override
    protected Void procesarRetorno(Retorno r, Void expr) {
        return null;
    }
}
