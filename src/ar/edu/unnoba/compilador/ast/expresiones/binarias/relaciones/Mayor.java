package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;

public class Mayor extends Relacion {
    public Mayor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, ">");
    }
}
