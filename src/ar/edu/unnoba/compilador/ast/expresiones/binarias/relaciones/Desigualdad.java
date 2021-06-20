package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Desigualdad extends Relacion {
    public Desigualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "!=");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo() == Tipo.INTEGER ? "ne" : "one";
    }

    @Override
    protected boolean calcularResultado(Float litIzq, Float litDer) {
        return !litIzq.equals(litDer);
    }
}
