package ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Booleano;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class NegacionLogica extends OperacionUnaria {
    public NegacionLogica(Expresion expresion) {
        super("NOT", expresion, Tipo.BOOLEAN);
    }

    public Expresion evaluar() {
        Expresion expr = getExpresion();
        if (expr instanceof NegacionLogica) {
            // cancelar "not not"
            return ((NegacionLogica) expr).getExpresion();
        }

        if (!(expr instanceof Booleano)) {
            return this;
        }

        boolean valorNegado = !((Booleano) getExpresion()).getValor();
        return new Booleano(valorNegado);
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }
}
