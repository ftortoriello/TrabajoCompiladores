package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Multiplicacion extends OperacionBinariaAritmetica {
    public Multiplicacion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "*");
    }

    @Override
    public String getInstruccionIR() {
        return getTipo() == Tipo.INTEGER ? "mul" : "fmul";
    }
}
