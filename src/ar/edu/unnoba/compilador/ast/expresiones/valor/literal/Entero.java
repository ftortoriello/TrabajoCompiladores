package ar.edu.unnoba.compilador.ast.expresiones.valor.literal;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Entero extends Literal {
    private final Integer valor;

    public Entero(Integer valor) {
        super(Tipo.INTEGER);
        this.valor = valor;
    }

    public Integer getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
