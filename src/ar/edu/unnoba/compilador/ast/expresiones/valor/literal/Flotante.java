package ar.edu.unnoba.compilador.ast.expresiones.valor.literal;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Flotante extends Literal {
    private final Double valor;

    public Flotante(Double valor) {
        super(Tipo.FLOAT);
        this.valor = valor;
    }

    public Double getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
