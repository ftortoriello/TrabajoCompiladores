package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.visitor.Transformer;

public abstract class Relacion extends OperacionBinaria {
    public Relacion(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }
}
