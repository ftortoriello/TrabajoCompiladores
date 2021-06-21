package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Cadena extends Expresion {

    private String valor;
    private String nombreIR;

    public Cadena(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Cadena accept(Transformer t) {
        return this;
    }

    public Expresion evaluar() {
        return this;
    }

    @Override
    public String toString() {
        return getValor();
    }
}
