package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Salir extends Control {
    public Salir() {
        super("BREAK");
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Salir accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
