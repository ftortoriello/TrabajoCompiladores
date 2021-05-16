package ar.edu.unnoba.compilador.ast.instrucciones;

import ar.edu.unnoba.compilador.ast.base.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class DeclaracionFuncion extends Sentencia {
    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        // TODO: implementar
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: implementar
        return null;
    }
}
