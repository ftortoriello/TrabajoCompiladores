package ar.edu.unnoba.compilador.ast.base.excepciones;

public class ExcepcionDeTipos extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionDeTipos(String mensaje) {
        super(mensaje);
    }
}
