package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Asignacion extends Sentencia {
    private Identificador ident;
    private Expresion expresion;

    public Asignacion(Identificador ident, Expresion e) {
        super("=");
        this.ident = ident;
        this.expresion = e;
    }

    public Identificador getIdent() {
        return ident;
    }

    public void setIdent(Identificador ident) {
        this.ident = ident;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    @Override
    public String getEtiqueta() {
        return getNombre();
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Asignacion accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
