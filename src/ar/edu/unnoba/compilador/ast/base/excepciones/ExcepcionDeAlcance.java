package ar.edu.unnoba.compilador.ast.base.excepciones;

public class ExcepcionDeAlcance extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionDeAlcance(String mensaje) {
        super(mensaje);
    }
}
