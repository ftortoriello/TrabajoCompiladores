package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class DecVarInicializada extends DecVar {

    private Expresion expresion;

    public DecVarInicializada(Identificador id, Expresion expr) {
        super(id);
        this.expresion = expr;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s[%s](%s = %s)",
                this.getClass().getSimpleName(), this.getId().getTipo(),
                this.getId().getEtiqueta(), this.getExpresion().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return null;
    }
}
