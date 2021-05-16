package ar.edu.unnoba.compilador.ast.instrucciones;

import ar.edu.unnoba.compilador.ast.base.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class DeclaracionVariable extends Sentencia {
    private Identificador id;
    private Tipo tipo;

    public DeclaracionVariable(Identificador id, Tipo tipo){
        this.id = id;
        this.tipo = tipo;
    }

    public Identificador getId() {
        return id;
    }

    public void setId(Identificador id) {
        this.id = id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public DeclaracionVariable accept_transfomer(Transformer t) {
        return t.transform(this);
    }
}
