package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

/** Declaración de variable o parámetro sin inicializar al declararse. */
public class DecVar extends Declaracion {
    public DecVar(Identificador ident) {
        super(ident);
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
