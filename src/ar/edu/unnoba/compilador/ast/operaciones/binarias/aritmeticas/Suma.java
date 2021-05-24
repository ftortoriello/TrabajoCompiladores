package ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Suma extends Adicion {

    public Suma(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, Tipo.UNKNOWN, "Suma");
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Suma accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
