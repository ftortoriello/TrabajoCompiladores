package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.visitor.Transformer;

public abstract class Sentencia extends Nodo {
    public Sentencia() {
    }

    public Sentencia(String nombre) {
        super(nombre);
    }
}
