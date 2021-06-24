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
        Expresion expresion = getExpresion().evaluar();
        if (!(expresion instanceof Literal)) {
            return this;
        }

        Number valor = ((Literal) expresion).getValorNumerico();
        if (expresion.getTipo().equals(Tipo.FLOAT)) {
            return new Literal(-valor.floatValue(), Tipo.FLOAT);
        } else {
            return new Literal(-valor.intValue(), Tipo.INTEGER);
        }
    }
}
