package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;

/* Clase para representar las variables en la tabla de símbolos.
 * Luego, mediante un Transformer, este nodo reemplaza a Identificador.
 */

public class SimboloVariable extends Identificador {
    // El nombre original, pero único y normalizado para usarse en IR.
    private String nombreIR;

    private final Boolean esGlobal;

    public SimboloVariable(Declaracion d, String nombreIR, Boolean esGlobal) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.nombreIR = nombreIR;
        this.esGlobal = esGlobal;
    }

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    public Boolean getEsGlobal() {
        return esGlobal;
    }
}
