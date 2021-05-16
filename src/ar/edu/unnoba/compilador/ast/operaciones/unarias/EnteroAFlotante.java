package ar.edu.unnoba.compilador.ast.operaciones.unarias;

import ar.edu.unnoba.compilador.ast.base.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("entero a flotante", expresion, Tipo.FLOAT);
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public EnteroAFlotante accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
