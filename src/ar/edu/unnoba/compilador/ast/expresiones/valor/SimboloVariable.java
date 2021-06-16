package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Clase para representar las variables en la tabla de símbolos.
 * Luego, mediante un Transformer, este nodo reemplaza a Identificador.
 */

public class SimboloVariable extends Identificador {

    // El nombre original, pero único y normalizado para usarse en IR.
    private String nombreIR;

    private Boolean esGlobal;

    public String getNombreIR() {
        return nombreIR;
    }

    public Boolean getEsGlobal() {
        return esGlobal;
    }

    public SimboloVariable(DecVar d, String nombreIR, Boolean esGlobal) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.nombreIR = nombreIR;
        this.esGlobal = esGlobal;
    }
}
