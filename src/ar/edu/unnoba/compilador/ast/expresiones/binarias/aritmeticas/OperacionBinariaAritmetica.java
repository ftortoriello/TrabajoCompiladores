package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public abstract class OperacionBinariaAritmetica extends OperacionBinaria {

    public OperacionBinariaAritmetica(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }

    @Override
    public Expresion evaluar() {
        if (!(getIzquierda().evaluar() instanceof Literal) || !(getDerecha().evaluar() instanceof Literal)) {
            return this;
        }

        float litIzq = Float.parseFloat(((Literal) getIzquierda().evaluar()).getValor());
        float litDer = Float.parseFloat(((Literal) getDerecha().evaluar()).getValor());
        float resultado = calcularResultado(litIzq, litDer);

        if (getIzquierda().getTipo() == Tipo.INTEGER) {
            return new Literal(Integer.toString((int)resultado), Tipo.INTEGER, "Literal INTEGER");
        } else {
            return new Literal(Float.toString(resultado), Tipo.FLOAT, "Literal FLOAT");
        }
    }

    private Float calcularResultado(Float fIzq, Float fDer) {
        switch (getNombre()) {
            case "+": return fIzq + fDer;
            case "-": return fIzq - fDer;
            case "*": return fIzq * fDer;
            case "/": return fIzq / fDer;
            default:
                throw new IllegalStateException("Operador inesperado al aplicar Constant Folding: " + getNombre());
        }
    }
}
