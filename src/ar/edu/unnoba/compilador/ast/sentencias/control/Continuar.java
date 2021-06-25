package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Continuar extends Control {
    public Continuar() {
        super("CONTINUE");
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Continuar accept(Transformer t) {
        return t.transform(this);
    }
}
