package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;

/* Clase para asociar las invocaciones a funciones a sus declaraciones.
 * Luego mediante un Transformer este nodo reemplaza a InvocacionFuncion.
 */
public class SimboloFuncion extends InvocacionFuncion {
    private DecFun declaracion;

    private final String nombreIR;

    public String getNombreIR() {
        return nombreIR;
    }

    public SimboloFuncion(DecFun d, String nombreIR) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.nombreIR = nombreIR;
        this.declaracion = d;
    }

    public DecFun getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecFun declaracion) {
        this.declaracion = declaracion;
    }
}
