package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Suma extends OperacionBinariaAritmetica {
    public Suma(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "+");
    }

    @Override
    public String getInstruccionIR() {
        return getTipo().equals(Tipo.FLOAT) ? "fadd" : "add";
    }

    @Override
    protected Number calcularResultado(Number izq, Number der) {
        return izq.doubleValue() + der.doubleValue();
    }

    @Override
    protected boolean esElementoNeutroIzq(Number numero) {
        return numero.equals(0);
    }
    @Override
    protected boolean esElementoNeutroDer(Number numero) {
        return numero.equals(0);
    }

    @Override
    protected boolean esElementoNuloIzq(Number numero) {
        return false;
    }
    @Override
    protected boolean esElementoNuloDer(Number numero) {
        return false;
    }
}
