package ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Entero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Flotante;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
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

        Number valor = Literal.getNumero(expresion);
        if (expresion.getTipo().equals(Tipo.FLOAT)) {
            return new Flotante(-valor.doubleValue());
        } else {
            return new Entero(-valor.intValue());
        }
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }
}
