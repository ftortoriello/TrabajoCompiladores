package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Mayor extends Relacion {
    public Mayor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, ">");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo().equals(Tipo.FLOAT) ? "ogt" : "sgt";
    }

    @Override
    protected boolean calcularResultado(double izq, double der) {
        return izq > der;
    }

    @Override
    protected boolean calcularResultado(boolean izq, boolean der) {
        // Esto ya viene validado desde el transformer de tipos
        throw new IllegalStateException("No se puede comparar entre BOOLEAN: " + izq + " > " + der);
    }
}
