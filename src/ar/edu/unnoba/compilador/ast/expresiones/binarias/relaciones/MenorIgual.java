package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;

public class MenorIgual extends Relacion {
    public MenorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "<=");
    }
}
