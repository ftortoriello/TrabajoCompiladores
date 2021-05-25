package ar.edu.unnoba.compilador.ast.operaciones.unarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class NegacionAritmetica extends OperacionUnaria {

    public NegacionAritmetica(Expresion expresion) {
        // TODO: se guarda la expresión pero no el menos, debería realizarse la conversion acá?
        super("Negacion aritmética", expresion);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
