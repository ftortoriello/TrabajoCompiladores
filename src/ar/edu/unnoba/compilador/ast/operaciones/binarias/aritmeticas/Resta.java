package ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Resta extends Adicion {
    public Resta(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, Tipo.UNKNOWN, "Resta");
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
