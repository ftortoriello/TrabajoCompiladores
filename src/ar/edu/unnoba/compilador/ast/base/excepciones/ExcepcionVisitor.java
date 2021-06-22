package ar.edu.unnoba.compilador.ast.base.excepciones;

import java.io.Serial;

public class ExcepcionVisitor extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExcepcionVisitor(String mensaje) {
        super(mensaje);
    }
}
