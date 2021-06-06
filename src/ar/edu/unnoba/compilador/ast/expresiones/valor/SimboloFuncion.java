package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;

/* Clase para asociar las invocaciones a funciones a sus declaraciones.
 * Luego mediante un Transformer este nodo reemplaza a InvocacionFuncion.
 */
public class SimboloFuncion extends InvocacionFuncion {
    private DecFuncion declaracion;

    public SimboloFuncion(DecFuncion d) {
        //super(d.getIdent().getNombre(), argumentos, d.getIdent().getTipo(), false);
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
    }

    public DecFuncion getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecFuncion declaracion) {
        this.declaracion = declaracion;
    }
}
