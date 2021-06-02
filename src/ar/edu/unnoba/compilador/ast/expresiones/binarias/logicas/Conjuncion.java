package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;

public class Conjuncion extends OperacionBinaria {
    public Conjuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "AND");
    }
}
