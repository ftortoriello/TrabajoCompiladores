package ar.edu.unnoba.compilador.excepciones;

import ar.edu.unnoba.compilador.ast.base.Nodo;

import java.io.Serial;

/** GestorExcepciones lanzadas por los visitors, por ejemplo errores de alcance. */
public class ExcepcionVisitor extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExcepcionVisitor(Nodo nodo, String mensaje) {
        super(String.format("%s «%s»: %s", nodo.getPosicion(), nodo.getNombre(), mensaje));
    }
}
