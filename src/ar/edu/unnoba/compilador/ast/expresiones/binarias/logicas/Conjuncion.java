package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;

public class Conjuncion extends OperacionBinariaLogica {
    public Conjuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "AND");
    }

    @Override
    public String getInstruccionIR() {
        return "and";
    }
}
