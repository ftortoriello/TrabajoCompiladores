package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Retorno extends Control {
    private Expresion expr;

    public Retorno(Expresion expr) {
        super("RETURN");
        this.expr = expr;
    }

    public Expresion getExpresion() {
        return expr;
    }

    public void setExpr(Expresion expr) {
        this.expr = expr;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Control accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
