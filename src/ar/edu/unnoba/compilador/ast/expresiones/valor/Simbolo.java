package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;

/* Clase para generar los alcances y validar los tipos.
 * Luego mediante un Transformer este nodo reemplaza a Identificador
 */
public class Simbolo extends Identificador {
    public Simbolo(Declaracion d) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
    }
}
