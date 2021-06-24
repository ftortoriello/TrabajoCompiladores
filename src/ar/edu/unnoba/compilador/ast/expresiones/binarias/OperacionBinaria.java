package ar.edu.unnoba.compilador.ast.expresiones.binarias;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public abstract class OperacionBinaria extends Expresion {
    private Expresion izquierda;
    private Expresion derecha;

    public OperacionBinaria(Expresion izquierda, Expresion derecha) {
        super(Tipo.UNKNOWN);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, String nombre) {
        super(Tipo.UNKNOWN, nombre);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, Tipo tipo) {
        super(tipo);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, Tipo tipo, String nombre) {
        super(tipo, nombre);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public void setIzquierda(Expresion izquierda) {
        this.izquierda = izquierda;
    }

    public void setDerecha(Expresion derecha) {
        this.derecha = derecha;
    }

    public Expresion getIzquierda() {
        return izquierda;
    }

    public Expresion getDerecha() {
        return derecha;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getIzquierda(), getNombre(), getDerecha());
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }

    public abstract String getInstruccionIR();
}
