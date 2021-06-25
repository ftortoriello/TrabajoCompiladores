package ar.edu.unnoba.compilador.excepciones;

import ar.edu.unnoba.compilador.ast.base.Nodo;

import java.io.Serial;

/**
 * GestorExcepciones lanzadas por los transformer, por ejemplo errores de tipos, de división por cero
 * y de compilación.
 */
public class ExcepcionTransformer extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExcepcionTransformer(Nodo nodo, String mensaje) {
        super(String.format("%s «%s»: %s", nodo.getPosicion(), nodo.getNombre(), mensaje));
    }
}
