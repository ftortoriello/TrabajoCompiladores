package ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class NegacionAritmetica extends OperacionUnaria {
    public NegacionAritmetica(Expresion expresion) {
        super("-", expresion);
    }
}
