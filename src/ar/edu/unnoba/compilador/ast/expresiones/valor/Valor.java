package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;

public abstract class Valor extends Expresion {
    public Valor() {
    }

    public Valor(Tipo tipo, String nombre) {
        super(tipo, nombre);
    }
}
