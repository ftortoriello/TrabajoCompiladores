package ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;

public abstract class Adicion extends OperacionBinaria {
    public Adicion(Expresion izquierda, Expresion derecha, Tipo tipo, String nombre) {
        super(izquierda, derecha, tipo, nombre);
    }
}
