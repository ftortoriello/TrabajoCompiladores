package ar.edu.unnoba.compilador.ast.expresiones.valor.literal;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Booleano extends Literal {
    private final Boolean valor;

    public Booleano(Boolean valor) {
        super(Tipo.BOOLEAN);
        this.valor = valor;
    }

    public Boolean getValor() {
        return valor;
    }

    @Override
    public String toString() {
        // Usamos las constantes de IR "true" y "false"
        return valor.toString();
    }
}
