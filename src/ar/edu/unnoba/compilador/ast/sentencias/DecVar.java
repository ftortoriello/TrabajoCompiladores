package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class DecVar extends Sentencia {
    private Identificador id;

    public DecVar(Identificador id){
        this.id = id;
    }

    public Identificador getId() {
        return id;
    }

    public void setId(Identificador id) {
        this.id = id;
    }

    public Tipo getTipo() {
        return this.getId().getTipo();
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s[%s](%s)",
                this.getClass().getSimpleName(), this.getId().getTipo(),
                this.getId().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public DecVar accept_transfomer(Transformer t) {
        return t.transform(this);
    }
}
