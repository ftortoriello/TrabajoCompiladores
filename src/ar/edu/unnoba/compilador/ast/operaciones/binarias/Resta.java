package ar.edu.unnoba.compilador.ast.operaciones.binarias;

import ar.edu.unnoba.compilador.ast.base.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Resta extends OperacionBinaria {
    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "-");
    }

    public Resta(Expresion izquierda, Expresion derecha, Tipo tipo) {
        super(izquierda, derecha, tipo, "-");
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Resta accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
