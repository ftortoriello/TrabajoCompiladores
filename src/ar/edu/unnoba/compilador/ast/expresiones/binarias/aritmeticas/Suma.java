package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Suma extends OperacionBinariaAritmetica {
    public Suma(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "+");
    }

    @Override
    public String getInstruccionIR() {
        return getTipo() == Tipo.INTEGER ? "add" : "fadd";
    }
}
