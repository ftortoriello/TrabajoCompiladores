package ar.edu.unnoba.compilador.ast.expresiones;

import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public abstract class Expresion extends Nodo {
    private Tipo tipo;
    private String refIR;

    public Expresion(Tipo tipo) {
        this.tipo = tipo;
    }

    public Expresion(String nombre) {
        super(nombre);
    }

    public Expresion(Tipo tipo, String nombre) {
        super(nombre);
        this.tipo = tipo;
    }

    public Expresion() {
        this.tipo = Tipo.UNKNOWN;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getRefIR() {
        if (refIR == null) {
            throw new IllegalStateException(String.format("Se hizo getRefIR sobre un objeto de tipo %s, " +
                            "pero el refIR es nulo.", getClass().getSimpleName()));
        }
        return refIR;
    }

    public void setRefIR(String refIR) {
        this.refIR = refIR;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\\n<%s>", getNombre(), getTipo());
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionTransformer {
        return null;
    }

    public abstract Expresion evaluar() throws ExcepcionTransformer;
}
