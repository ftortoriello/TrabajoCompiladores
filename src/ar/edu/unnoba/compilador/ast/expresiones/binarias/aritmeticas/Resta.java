package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class Resta extends OperacionBinariaAritmetica {
    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "-");
    }

    @Override
    public String getInstruccionIR() {
        return ((this.getTipo() == Tipo.INTEGER) ? "sub" : "fsub");
    }
}
