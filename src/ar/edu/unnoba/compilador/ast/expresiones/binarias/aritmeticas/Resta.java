package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Resta extends OperacionBinariaAritmetica {
    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "-");
    }

    @Override
    public String getInstruccionIR() {
        return ((this.getTipo() == Tipo.INTEGER) ? "sub" : "fsub");
    }
}
