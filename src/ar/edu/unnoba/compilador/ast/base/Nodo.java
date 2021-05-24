package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.*;

public abstract class Nodo {
    private String nombre;

    public Nodo() {}

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

    public abstract <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance;

    public abstract <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos;
}