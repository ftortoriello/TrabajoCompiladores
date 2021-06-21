package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class MayorIgual extends Relacion {
    public MayorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, ">=");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo() == Tipo.FLOAT ? "oge" : "sge";
    }

    @Override
    protected boolean calcularResultado(float izq, float der) {
        return izq >= der;
    }

    @Override
    protected boolean calcularResultado(boolean izq, boolean der) {
        throw new IllegalStateException("No se puede comparar entre BOOLEAN: " + izq + " >= " + der);
    }
}
