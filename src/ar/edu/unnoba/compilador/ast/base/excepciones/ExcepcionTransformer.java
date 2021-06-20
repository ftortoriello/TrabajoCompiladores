package ar.edu.unnoba.compilador.ast.base.excepciones;

/* Clase que representa excepciones de tipos, aritméticas (división por cero) y de compilación */

public class ExcepcionTransformer extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionTransformer(String mensaje) {
        super(mensaje);
    }
}
