package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;

/* Clase para representar las variables en la tabla de símbolos.
 * Luego, mediante un Transformer, este nodo reemplaza a Identificador.
 */

public class SimboloVariable extends Identificador {

    private String nombreIR;

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    public SimboloVariable(DecVar d) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
    }
}
