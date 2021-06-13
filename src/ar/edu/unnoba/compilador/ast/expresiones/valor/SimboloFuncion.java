package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;

/* Clase para asociar las invocaciones a funciones a sus declaraciones.
 * Luego mediante un Transformer este nodo reemplaza a InvocacionFuncion.
 */
public class SimboloFuncion extends InvocacionFuncion {
    private DecFuncion declaracion;

    private String nombreIR;

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    public SimboloFuncion(DecFuncion d, String nombreIR) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.nombreIR = nombreIR;
        this.declaracion = d;
    }

    public DecFuncion getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecFuncion declaracion) {
        this.declaracion = declaracion;
    }
}
