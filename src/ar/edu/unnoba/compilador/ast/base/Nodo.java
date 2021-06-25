package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class Nodo {
    private String nombre;

    public Nodo() {
    }

    public Nodo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEtiqueta() {
        if (this.nombre != null) {
            return this.getNombre();
        }
        return this.getClass().getSimpleName();
    }

    public abstract void accept(Visitor v) throws ExcepcionVisitor;

    public abstract Nodo accept(Transformer t) throws ExcepcionTransformer;

    @Override
    public String toString() {
        if (nombre == null) {
            return super.toString();
        } else {
            return nombre;
        }
    }
}