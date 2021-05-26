package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Variable extends Valor {
    // TODO: reemplaza al nodo Identificador en una 2da pasada
    private DecVar declaracion;

    public Variable(DecVar declaracion) {
        this.declaracion = declaracion;
    }

    public DecVar getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecVar declaracion) {
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
