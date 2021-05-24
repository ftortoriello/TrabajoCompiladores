package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.ast.base.Nodo;

public abstract class Sentencia extends Nodo {
    public Sentencia() {
    }

    public Sentencia(String nombre) {
        super(nombre);
    }
}
