package ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class NegacionLogica extends OperacionUnaria {
    public NegacionLogica(Expresion expresion) {
        super("NOT", expresion);
    }

    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion();
        if (expr instanceof NegacionLogica) {
            // cancelar "not not"
            return ((NegacionLogica) expr).getExpresion();
        }

        if (!(expr instanceof Literal)) {
            // TODO si no es literal se rompe
            return this;
        }

        boolean valorNegado = !((Literal) getExpresion()).getValorBooleano();
        return new Literal(String.valueOf(valorNegado), Tipo.BOOLEAN);
    }
}
