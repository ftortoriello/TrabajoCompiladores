package ar.edu.unnoba.compilador.ast.base.excepciones;

public class ExcepcionVisitor extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionVisitor(String mensaje) {
        super(mensaje);
    }
}
