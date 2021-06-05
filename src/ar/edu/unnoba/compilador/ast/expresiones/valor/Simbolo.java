package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Clase para generar los alcances y validar los tipos.
 * Luego mediante un Transformer este nodo reemplaza a Identificador
 */
public class Simbolo extends Identificador {
    public Simbolo(Declaracion d) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
    }

    @Override
    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public Simbolo accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
