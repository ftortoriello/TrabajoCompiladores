package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class Division extends OperacionBinaria {
    public Division(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "/");
    }

    public Division(Expresion izquierda, Expresion derecha, Tipo tipo) {
        super(izquierda, derecha, tipo, "/");
    }

    @Override
    public Expresion evaluar() {
        if (!(getIzquierda().evaluar() instanceof Literal) || !(getDerecha().evaluar() instanceof Literal)) {
            return this;
        }

        float litIzq = Float.parseFloat(((Literal) getIzquierda().evaluar()).getValor());
        float litDer = Float.parseFloat(((Literal) getDerecha().evaluar()).getValor());
        float resultado = litIzq / litDer;

        if (getIzquierda().getTipo() == Tipo.INTEGER) {
            return new Literal(Integer.toString((int)resultado), Tipo.INTEGER, "Literal INTEGER");
        } else {
            return new Literal(Float.toString(resultado), Tipo.FLOAT, "Literal FLOAT");
        }
    }
}
