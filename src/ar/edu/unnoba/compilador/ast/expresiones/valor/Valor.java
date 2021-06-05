package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public abstract class Valor extends Expresion {
    public Valor(Tipo tipo, String nombre) {
        super(tipo, nombre);
    }
}
