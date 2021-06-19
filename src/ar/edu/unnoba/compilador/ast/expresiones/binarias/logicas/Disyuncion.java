package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;

public class Disyuncion extends OperacionBinariaLogica {
    public Disyuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "OR");
    }

    @Override
    public String getInstruccionIR() {
        return "or";
    }

    @Override
    protected Boolean calcularResultado(Boolean litIzq, Boolean litDer) {
        return litIzq || litDer;
    }

}
