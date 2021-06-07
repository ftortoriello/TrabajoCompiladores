package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("Entero a flotante", expresion, Tipo.FLOAT);
    }

    public Expresion evaluar() {
        return getExpresion().evaluar();
    }
}
