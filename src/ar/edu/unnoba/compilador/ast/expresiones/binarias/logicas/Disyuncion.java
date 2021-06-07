package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class Disyuncion extends OperacionBinaria {
    public Disyuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "OR");
    }

    @Override
    public Expresion evaluar() {
        if (!(getIzquierda().evaluar() instanceof Literal) || !(getDerecha().evaluar() instanceof Literal)) {
            return this;
        }

        boolean litIzq = Boolean.parseBoolean(((Literal) getIzquierda().evaluar()).getValor());
        boolean litDer = Boolean.parseBoolean(((Literal) getDerecha().evaluar()).getValor());
        boolean resultado = litIzq || litDer ;
        return new Literal(String.valueOf(resultado), Tipo.BOOLEAN, "Literal BOOLEAN");
    }
}
