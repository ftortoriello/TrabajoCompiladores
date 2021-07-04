package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;

/**
 * Clase para representar las variables en la tabla de símbolos.
 * Luego, mediante un Transformer, este nodo reemplaza a Identificador.
 */

public class SimboloVariable extends Identificador {
    /** El nombre original, pero único y normalizado para usarse en IR. */
    private String ptroIR;

    private final Boolean esGlobal;

    public SimboloVariable(Declaracion d, Boolean esGlobal) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.esGlobal = esGlobal;
    }

    public String getPtroIR() {
        return ptroIR;
    }

    public void setPtroIR(String ptroIR) {
        this.ptroIR = ptroIR;
    }

    public Boolean getEsGlobal() {
        return esGlobal;
    }
}
