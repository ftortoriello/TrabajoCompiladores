package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Clase para generar los alcances y validar los tipos.
 * TODO: Luego mediante un Transformer este nodo reemplaza a Identificador
 */
public class Simbolo extends Valor {
    private Declaracion declaracion;

    public Simbolo(Declaracion declaracion) {
        this.declaracion = declaracion;
    }

    public Declaracion getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(Declaracion declaracion) {
        this.declaracion = declaracion;
    }

    public Tipo getTipo() {
        return declaracion.getTipo();
    }

    public void setTipo(Tipo tipo) {
        declaracion.setTipo(tipo);
    }

    @Override
    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public Simbolo accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    @Override
    public String toString() {
        return declaracion.toString();
    }
}
