package ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class NegacionLogica extends OperacionUnaria {
    public NegacionLogica(Expresion expresion) {
        super("NOT", expresion);
    }
}
