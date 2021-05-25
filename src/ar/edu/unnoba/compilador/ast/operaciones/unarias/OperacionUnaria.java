package ar.edu.unnoba.compilador.ast.operaciones.unarias;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;

public abstract class OperacionUnaria extends Expresion{
    private Expresion expresion;

    public OperacionUnaria(Expresion expresion) {
        super(Tipo.UNKNOWN);
        this.expresion = expresion;
    }

    public OperacionUnaria(String nombre, Expresion expresion) {
        super(Tipo.UNKNOWN, nombre);
        this.expresion = expresion;
    }

    public OperacionUnaria(Expresion expresion, Tipo tipo) {
        super(tipo);
        this.expresion = expresion;
    }

    public OperacionUnaria(String nombre, Expresion expresion, Tipo tipo) {
        super(tipo, nombre);
        this.expresion = expresion;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s[%s](%s)",
                this.getClass().getSimpleName(), this.getTipo(),
                this.getExpresion().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }
}
