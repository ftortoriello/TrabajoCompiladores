package ar.edu.unnoba.compilador.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
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

        SimboloVariable simboloExistente = alcanceGlobal.putIfAbsent(nombre, new SimboloVariable(d));
        if (simboloExistente != null) {
            throw new ExcepcionDeAlcance(
                    String.format("La variable global «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            simboloExistente.getTipo()));
        }
    }

    private void agregarSimboloFuncion(DecFuncion d) throws ExcepcionDeAlcance {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        SimboloFuncion simboloExistente = tablaFunciones.putIfAbsent(nombre, new SimboloFuncion(d));
        if (simboloExistente != null) {
            throw new ExcepcionDeAlcance(
                    String.format("La función «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            simboloExistente.getTipo()));
        }
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
    protected Void procesarVarInicializada(Void ident, Void expr) {
        return null;
    }

    @Override
    protected Void procesarAsignacion(Asignacion a, Void identificador, Void expresion) {
        return null;
    }

    @Override
    protected Void procesarDecFuncion(List<Void> args, Void cuerpo) {
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
