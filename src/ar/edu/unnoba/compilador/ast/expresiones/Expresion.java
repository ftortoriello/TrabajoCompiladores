package ar.edu.unnoba.compilador.ast.expresiones;

import ar.edu.unnoba.compilador.ast.base.Nodo;

public abstract class Expresion extends Nodo {
    // TODO: renombrar operaciones por "expresiones", mover esta clase para allá
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
        return String.format("%s", this.getNombre());
    }

    @Override
    public String toString() {
        return this.getEtiqueta();
    }
}