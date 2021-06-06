package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;

/* Clase para representar las variables en la tabla de símbolos.
 * Luego, mediante un Transformer, este nodo reemplaza a Identificador.
 */

public class SimboloVariable extends Identificador {
    public SimboloVariable(Declaracion d) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
    }
}
