package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;

public class Resta extends OperacionBinaria {
    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, Tipo.UNKNOWN, "-");
    }
}
