package ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class NegacionAritmetica extends OperacionUnaria {
    public NegacionAritmetica(Expresion expresion) {
        super("-", expresion);
    }

    public Expresion evaluar() throws ExcepcionTransformer {
        if (!(getExpresion() instanceof Literal)) {
            return this;
        }

        if (getExpresion().getTipo() == Tipo.INTEGER) {
            String valorNegado = String.valueOf(Integer.parseInt(getExpresion().evaluar().toString()) * -1);
            return new Literal(valorNegado, Tipo.INTEGER);
        } else {
            String valorNegado = String.valueOf(Float.parseFloat(getExpresion().evaluar().toString()) * -1);
            return new Literal(valorNegado, Tipo.FLOAT);
        }
    }
}
