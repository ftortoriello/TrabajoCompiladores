package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.instrucciones.DeclaracionVariable;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Variable extends Expresion {

    private DeclaracionVariable declaracion;

    public Variable(DeclaracionVariable declaracion){
        this.declaracion = declaracion;
    }

    public DeclaracionVariable getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DeclaracionVariable declaracion) {
        this.declaracion = declaracion;
    }

    @Override
    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public Variable accept_transfomer(Transformer t) {
        return t.transform(this);
    }

}
