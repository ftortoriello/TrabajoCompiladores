package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

/** Parámetro de una función sin valor por defecto. */
public class Param extends Declaracion {
    public Param(String nombre, Identificador ident) {
        super(nombre, ident);
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Param accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
