package ar.edu.unnoba.compilador.visitor;

import java.util.HashMap;
import java.util.Map;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;

/* Visitor para generar el alcance global y construir la tabla de símbolos
 * globales.
 */
public class GeneradorDeAlcanceGlobal extends Visitor {
    private Alcance alcanceGlobal;
    private Map<String, SimboloFuncion> tablaFunciones;

    // Agregar el símbolo de la variable al ámbito global
    private void agregarSimboloVarGlobal(Declaracion d) throws ExcepcionDeAlcance {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (alcanceGlobal.containsKey(nombre)) {
            throw new ExcepcionDeAlcance(
                    String.format("La variable global «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            alcanceGlobal.get(nombre).getTipo()));
        }

        // Defino de antemano los nombres que necesitamos para el IR
        String nombreIR = Normalizador.crearNomPtroGbl(nombre);
        Boolean esGlobal = true;

        SimboloVariable simbolo = new SimboloVariable(d, nombreIR, esGlobal);

        alcanceGlobal.put(nombre, simbolo);
    }

    private void agregarSimboloFuncion(DecFun d) throws ExcepcionDeAlcance {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (tablaFunciones.containsKey(nombre)) {
            throw new ExcepcionDeAlcance(
                    String.format("La función «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(), tablaFunciones.get(nombre).getTipo()));
        }

        String nombreIR = Normalizador.crearNomFun(nombre);
        SimboloFuncion simbolo = new SimboloFuncion(d, nombreIR);
        tablaFunciones.put(nombre, simbolo);
    }

    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
        alcanceGlobal = new Alcance("global");
        p.setAlcance(alcanceGlobal);

        tablaFunciones = new HashMap<>();
        p.setTablaFunciones(tablaFunciones);

        super.visit(p);
    }

    // No procesar los bloques en este Visitor
    @Override
    public void visit(Bloque b) {
    }

    @Override
    public void visit(DecFun df) throws ExcepcionDeAlcance {
        // Agregar a la tabla la declaración de la función
        agregarSimboloFuncion(df);
        // No visitar los parámetros ni el cuerpo
    }

    // Visit de declaraciones de variables globales (en el encabezado).
    @Override
    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        agregarSimboloVarGlobal(dv);
        super.visit(dv);
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionDeAlcance {
        if (dvi.getExpresion() instanceof InvocacionFuncion) {
            throw new ExcepcionDeAlcance(String.format(
                    "«%s»: No se puede invocar a una función desde la inicialización de una variable global.",
                    dvi.getIdent()));
        }
        agregarSimboloVarGlobal(dvi);
        super.visit(dvi);
    }
}
