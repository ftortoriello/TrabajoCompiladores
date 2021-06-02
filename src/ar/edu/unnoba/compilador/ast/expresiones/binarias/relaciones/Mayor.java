package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class Mayor extends Relacion {
    public Mayor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, ">");
    }
}
