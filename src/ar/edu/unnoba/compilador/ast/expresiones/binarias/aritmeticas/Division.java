package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Division extends OperacionBinariaAritmetica {
    public Division(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "/");
    }

    @Override
    public String getInstruccionIR() {
        return getTipo() == Tipo.INTEGER ? "sdiv" : "fdiv";
    }

    @Override
    protected Number calcularResultado(Number izq, Number der) throws ExcepcionTransformer {
        if (der.equals(0)) throw new ExcepcionTransformer("División por cero");
        return izq.doubleValue() / der.doubleValue();
    }

    @Override
    protected boolean esElementoNeutroIzq(Number numero) {
        return false;
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
        return false;
    }
}
