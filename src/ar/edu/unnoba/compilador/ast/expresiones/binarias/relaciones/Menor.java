package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Menor extends Relacion {
    public Menor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "<");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo().equals(Tipo.FLOAT) ? "olt" : "slt";
    }

    @Override
    protected boolean calcularResultado(double izq, double der) {
        return izq < der;
    }

    @Override
    protected boolean calcularResultado(boolean izq, boolean der) {
        throw new IllegalStateException("No se puede comparar entre BOOLEAN: " + izq + " < " + der);
    }
}
