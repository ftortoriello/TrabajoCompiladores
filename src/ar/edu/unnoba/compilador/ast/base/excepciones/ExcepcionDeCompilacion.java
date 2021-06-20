package ar.edu.unnoba.compilador.ast.base.excepciones;

public class ExcepcionDeCompilacion extends Exception {
    private static final long serialVersionUID = 1L;

    public ExcepcionDeCompilacion(String mensaje){
        super(mensaje);
    }
}
