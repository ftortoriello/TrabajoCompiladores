package ar.edu.unnoba.compilador.ast.expresiones;

import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public abstract class Expresion extends Nodo {
    private Tipo tipo;

    public Expresion(Tipo tipo) {
        this.tipo = tipo;
    }

    public Expresion(String nombre) {
        super(nombre);
    }

    public Expresion(Tipo tipo, String nombre) {
        super(nombre);
        this.tipo = tipo;
    }

    public Expresion() {
        this.tipo = Tipo.UNKNOWN;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\n<%s>", getNombre(), getTipo());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return null;
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionDeTipos {
        return null;
    }

    public abstract Expresion evaluar();
}
