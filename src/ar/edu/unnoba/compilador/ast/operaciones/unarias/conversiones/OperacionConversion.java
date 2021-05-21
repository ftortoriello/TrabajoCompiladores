package ar.edu.unnoba.compilador.ast.operaciones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.OperacionUnaria;

public abstract class OperacionConversion extends OperacionUnaria {

    public OperacionConversion(Expresion expresion) {
        super(expresion);
    }

    public OperacionConversion(String nombre, Expresion expresion) {
        super(nombre, expresion);
    }

    public OperacionConversion(Expresion expresion, Tipo tipo) {
        super(expresion, tipo);
    }

    public OperacionConversion(String nombre, Expresion expresion, Tipo tipo) {
        super(nombre, expresion, tipo);
    }

}

