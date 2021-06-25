package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

/* Clase para catalogar en la primera pasada cosas que no sabemos si son
 * variables, funciones...
 * Después se reemplazan por Simbolo. */
public class Identificador extends Valor {
    public Identificador(String nombre) {
        super(Tipo.UNKNOWN, nombre);
    }

    public Identificador(String nombre, Tipo tipo) {
        super(tipo, nombre);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\\n<%s>", getNombre(), getTipo());
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Identificador accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }

    public Expresion evaluar() {
        return this;
    }
}
