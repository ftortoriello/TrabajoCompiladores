package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Declaración de variable o parámetro sin inicializar. */
public class DecVar extends Declaracion {
    // Clase para las variables que no fueron inicializadas al crearse.

    public DecVar(String nombre, Identificador ident) {
        super(nombre, ident);
    }

    @Override
    public String toString() {
        return getIdent().getNombre();
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public DecVar accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
