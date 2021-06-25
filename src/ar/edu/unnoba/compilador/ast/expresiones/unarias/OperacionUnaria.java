package ar.edu.unnoba.compilador.ast.expresiones.unarias;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class OperacionUnaria extends Expresion {
    private Expresion expresion;

    public OperacionUnaria(String nombre, Expresion expresion) {
        super(Tipo.UNKNOWN, nombre);
        this.expresion = expresion;
    }

    public OperacionUnaria(String nombre, Expresion expresion, Tipo tipo) {
        super(tipo, nombre);
        this.expresion = expresion;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public String toString() {
        return String.format("%s %s", getNombre(), getExpresion());
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
