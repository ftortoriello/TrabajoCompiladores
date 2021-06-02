package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("Entero a flotante", expresion, Tipo.FLOAT);
    }
}
