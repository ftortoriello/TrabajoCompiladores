package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Disyuncion extends OperacionBinaria {
    public Disyuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "OR");
    }
}
