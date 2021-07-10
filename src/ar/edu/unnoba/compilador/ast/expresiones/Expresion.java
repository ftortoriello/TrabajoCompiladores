package ar.edu.unnoba.compilador.ast.expresiones;

import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class Expresion extends Nodo {
    private Tipo tipo;
    private String refIR;

    /**
     * Propiedad para tratar de manera especial a las operaciones lógicas binarias y unarias
     * que sean la condición de una estructura. Se pone en esta clase para evitar casteos e ifs.
     */
    private boolean enCortocircuito = false;

    public Expresion() {
        this.tipo = Tipo.UNKNOWN;
    }

    public Expresion(Tipo tipo) {
        this.tipo = tipo;
    }

    public Expresion(Tipo tipo, String nombre) {
        super(nombre);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getRefIR() {
        if (refIR == null) {
            throw new IllegalStateException(String.format(
                    "Se hizo getRefIR sobre un objeto de tipo %s, pero refIR es nulo.",
                    getClass().getSimpleName()));
        }
        return refIR;
    }

    public void setRefIR(String refIR) {
        this.refIR = refIR;
    }

    public boolean getEnCortocircuito() {
        return enCortocircuito;
    }

    public void setEnCortocircuito(boolean enCortocircuito) {
        this.enCortocircuito = enCortocircuito;
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
