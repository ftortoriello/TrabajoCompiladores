package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class Disyuncion extends OperacionBinariaLogica {
    public Disyuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "OR");
    }

    @Override
    public String getInstruccionIR() {
        return "or";
    }

    @Override
    protected Expresion transformarResultado(boolean valor, Expresion otroOperando) {
        return valor == true ? new Literal(true, Tipo.BOOLEAN) : otroOperando;
    }
}
