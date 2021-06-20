package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

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
        return valor == true ? otroOperando : new Literal(false, Tipo.BOOLEAN);
    }
}
