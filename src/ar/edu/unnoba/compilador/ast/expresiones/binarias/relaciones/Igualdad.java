package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Igualdad extends Relacion {
    public Igualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "==");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo() == Tipo.INTEGER ? "eq" : "oeq";
    }

    @Override
    protected boolean calcularResultado(Float litIzq, Float litDer) {
        return litIzq.equals(litDer);
    }
}
