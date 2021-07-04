package ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;

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
            return new Literal(-valor.doubleValue(), Tipo.FLOAT);
        } else {
            return new Literal(-valor.intValue(), Tipo.INTEGER);
        }
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }
}
