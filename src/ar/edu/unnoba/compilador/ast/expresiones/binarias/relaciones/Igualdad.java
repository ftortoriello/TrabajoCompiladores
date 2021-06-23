package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Igualdad extends Relacion {
    public Igualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "==");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo().equals(Tipo.FLOAT) ? "oeq" : "eq";
    }

    @Override
    protected boolean calcularResultado(float izq, float der) {
        return izq == der;
    }

    @Override
    protected boolean calcularResultado(boolean izq, boolean der) {
        return izq == der;
    }
}
