package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;

public class MayorIgual extends Relacion {
    public MayorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, ">=");
    }
}
