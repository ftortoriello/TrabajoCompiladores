package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.Transformer;

/* Clase para catalogar en la primera pasada cosas que no sabemos si son
 * variables, funciones...
 * Después se reemplazan por Simbolo cuando se arman las tablas de simbolos. */
public class Identificador extends Valor {
    public Identificador(String nombre) {
        super(Tipo.UNKNOWN, nombre);
    }

    public Identificador(String nombre, Tipo tipo) {
        super(tipo, nombre);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\n<%s>", getNombre(), getTipo());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Identificador accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
