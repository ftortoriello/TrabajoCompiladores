package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class Constante extends Expresion {
    private final String valor;

    public Constante(String valor) {
        this.valor = valor;
    }

    public Constante(String valor, String nombre) {
        super(nombre);
        this.valor = valor;
    }

    public Constante(String valor, Tipo tipo, String nombre) {
        super(tipo, nombre);
        this.valor = valor;
    }

    public Constante(String valor, Tipo tipo) {
        super(tipo);
        this.valor = valor;
    }

    public Object getValor() {
        return valor;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("Const %s %s", getValor(), this.getTipo()));
    }

    @Override
    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public Constante accept_transfomer(Transformer t) {
        return t.transform(this);
    }
}
