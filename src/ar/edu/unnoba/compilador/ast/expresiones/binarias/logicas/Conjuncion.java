package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Booleano;

public class Conjuncion extends OperacionBinariaLogica {
    public Conjuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "AND");
    }

    @Override
    public String getInstruccionIR() {
        return "and";
    }

    @Override
    protected Expresion transformarResultado(boolean valor, Expresion otroOperando) {
        return valor ? otroOperando : new Booleano(false);
    }
}
