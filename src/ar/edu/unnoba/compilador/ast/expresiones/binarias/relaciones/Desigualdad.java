package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Desigualdad extends Relacion {
    public Desigualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "!=");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo().equals(Tipo.FLOAT) ? "one" : "ne";
    }

    @Override
    protected boolean calcularResultado(double litIzq, double litDer) {
        return litIzq != litDer;
    }

    @Override
    protected boolean calcularResultado(boolean izq, boolean der) {
        return izq != der;
    }
}
