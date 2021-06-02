package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class Desigualdad extends Relacion {
    public Desigualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "!=");
    }
}
