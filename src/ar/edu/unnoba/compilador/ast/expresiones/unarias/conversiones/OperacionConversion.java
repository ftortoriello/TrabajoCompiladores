package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;

public abstract class OperacionConversion extends OperacionUnaria {
    public OperacionConversion(Expresion expresion) {
        super(expresion);
    }

    public OperacionConversion(String nombre, Expresion expresion, Tipo tipo) {
        super(nombre, expresion, tipo);
    }
}
