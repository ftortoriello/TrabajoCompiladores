package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Visitor para generar el alcance global y construir la tabla de símbolos globales. */
public class GeneradorDeAlcanceGlobal extends Visitor {
    private Alcance alcanceGlobal;
    private Map<String, DecFun> tablaFunciones;

    /** Agregar el símbolo de la variable al ámbito global. */
    private void agregarSimboloVarGlobal(Declaracion d) throws ExcepcionVisitor {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (alcanceGlobal.containsKey(nombre)) {
            throw new ExcepcionVisitor(d, String.format(
                    "La variable global de tipo %s ya fue declarada previamente con tipo %s.",
                    id.getTipo(), alcanceGlobal.get(nombre).getTipo()));
        }

        SimboloVariable sv = new SimboloVariable(d, true);
        alcanceGlobal.put(nombre, sv);
    }

    private void agregarSimboloFuncion(DecFun df) throws ExcepcionVisitor {
        Identificador id = df.getIdent();
        String nombreFun = id.getNombre();

        if (tablaFunciones.containsKey(nombreFun)) {
            throw new ExcepcionVisitor(df, String.format(
                    "La función de tipo %s ya fue declarada previamente con tipo %s.",
                    id.getTipo(), tablaFunciones.get(nombreFun).getTipo()));
        }

        tablaFunciones.put(nombreFun, df);
    }

    private void agregarFuncionPredefinida(String nombre, Tipo tipo) {
        Identificador id = new Identificador(nombre, tipo);
        DecFun d = new DecFun(id, new ArrayList<>(), new Bloque(nombre, false));
        tablaFunciones.put(nombre, d);
    }

    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
        p.setAlcance(new Alcance("global"));
        alcanceGlobal = p.getAlcance();

        p.setTablaFunciones(new HashMap<>());
        tablaFunciones = p.getTablaFunciones();

        // Agregar funciones predefinidas a la tabla de funciones
        agregarFuncionPredefinida("read_boolean", Tipo.BOOLEAN);
        agregarFuncionPredefinida("read_integer", Tipo.INTEGER);
        agregarFuncionPredefinida("read_float", Tipo.FLOAT);

        // No es necesario agregar estas, pero para que quede completo
        agregarFuncionPredefinida("write", Tipo.UNKNOWN);
        agregarFuncionPredefinida("writeln", Tipo.UNKNOWN);

        super.visit(p);
    }

    // No procesar los bloques en este Visitor
    @Override
    public void visit(Bloque b) {
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        // Agregar a la tabla la declaración de la función, sin visitar los parámetros ni el cuerpo
        agregarSimboloFuncion(df);
    }

    // Visit de declaraciones de variables globales (en el encabezado).
    @Override
    public void visit(DecVar dv) throws ExcepcionVisitor {
        agregarSimboloVarGlobal(dv);
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        if (dvi.getExpresion() instanceof InvocacionFuncion) {
            throw new ExcepcionVisitor(dvi,
                    "No se puede invocar a una función desde la inicialización de una variable global.");
        }
        agregarSimboloVarGlobal(dvi);
    }
}
