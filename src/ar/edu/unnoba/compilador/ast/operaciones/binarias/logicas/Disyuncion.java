package ar.edu.unnoba.compilador.ast.operaciones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Disyuncion extends OperacionBinaria {

    public Disyuncion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "OR");
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
