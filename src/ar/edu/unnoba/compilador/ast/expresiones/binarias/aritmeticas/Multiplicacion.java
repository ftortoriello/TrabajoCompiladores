package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Multiplicacion extends OperacionBinariaAritmetica {
    public Multiplicacion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "*");
    }

    @Override
    public String getInstruccionIR() {
        return getTipo().equals(Tipo.FLOAT) ? "fmul" : "mul";
    }

    @Override
    protected Number calcularResultado(Number izq, Number der) {
        return izq.floatValue() * der.floatValue();
    }

    @Override
    protected boolean esElementoNeutroIzq(Number numero) {
        return numero.equals(1);
    }
    @Override
    protected boolean esElementoNeutroDer(Number numero) {
        return numero.equals(1);
    }

    @Override
    protected boolean esElementoNuloIzq(Number numero) {
        return numero.equals(0);
    }
    @Override
    protected boolean esElementoNuloDer(Number numero) {
        return numero.equals(0);
    }
}
