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
        if (!(getExpresion() instanceof Literal)) {
            return this;
        }

        boolean valorNegado = !((Literal) getExpresion().evaluar()).getValorBooleano();
        return new Literal(String.valueOf(valorNegado), Tipo.BOOLEAN);
    }
}
