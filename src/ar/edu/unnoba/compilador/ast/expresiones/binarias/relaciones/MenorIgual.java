package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class MenorIgual extends Relacion {
    public MenorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "<=");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo() == Tipo.INTEGER ? "sle" : "ole";
    }

    @Override
    protected boolean calcularResultado(Float litIzq, Float litDer) {
        return litIzq <= litDer;
    }
}
