package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class Menor extends Relacion {
    public Menor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "<");
    }

    public String getInstruccionIR() {
        return getIzquierda().getTipo() == Tipo.INTEGER ? "slt" : "olt";
    }
}
