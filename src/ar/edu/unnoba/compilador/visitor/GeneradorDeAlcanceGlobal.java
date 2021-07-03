package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.util.Normalizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Visitor para generar el alcance global y construir la tabla de símbolos globales. */
public class GeneradorDeAlcanceGlobal extends Visitor {
    private Alcance alcanceGlobal;
    private Map<String, SimboloFuncion> tablaFunciones;

    /** Agregar el símbolo de la variable al ámbito global. */
    private void agregarSimboloVarGlobal(Declaracion d) throws ExcepcionVisitor {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (alcanceGlobal.containsKey(nombre)) {
            throw new ExcepcionVisitor(d, String.format(
                    "La variable global de tipo %s ya fue declarada previamente con tipo %s.",
                    id.getTipo(), alcanceGlobal.get(nombre).getTipo()));
        }

        // Defino de antemano los nombres que necesitamos para el IR
        final String nombreIR = Normalizador.crearNomPtroGbl(nombre);
        final boolean esGlobal = true;

        SimboloVariable simbolo = new SimboloVariable(d, nombreIR, esGlobal);

        alcanceGlobal.put(nombre, simbolo);
    }

    private void agregarSimboloFuncion(DecFun d) throws ExcepcionVisitor {
        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        if (tablaFunciones.containsKey(nombre)) {
            throw new ExcepcionVisitor(d, String.format(
                    "La función de tipo %s ya fue declarada previamente con tipo %s.",
                    id.getTipo(), tablaFunciones.get(nombre).getTipo()));
        }

        String nombreFun = Normalizador.crearNomFun(nombre);
        String ptroRet = Normalizador.crearNomPtroLcl("ret");
        SimboloFuncion simbolo = new SimboloFuncion(d, nombreFun, ptroRet);
        simbolo.setRefIR(Normalizador.crearNomRef("ret"));
        tablaFunciones.put(nombre, simbolo);
    }

    private void agregarFuncionPredefinida(String nombre, Tipo tipo) {
        Identificador id = new Identificador(nombre, tipo);
        DecFun d = new DecFun(id, new ArrayList<>(), new Bloque(nombre, false));
        SimboloFuncion simbolo = new SimboloFuncion(d, "@" + nombre,
                Normalizador.crearNomPtroLcl("ret"));
        tablaFunciones.put(nombre, simbolo);
    }

    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
        alcanceGlobal = new Alcance("global");
        p.setAlcance(alcanceGlobal);

        tablaFunciones = new HashMap<>();
        p.setTablaFunciones(tablaFunciones);

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
        // Agregar a la tabla la declaración de la función
        agregarSimboloFuncion(df);
        df.setEtiquetaFin(Normalizador.crearNomEtiqueta("ret"));
        // No visitar los parámetros ni el cuerpo
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
