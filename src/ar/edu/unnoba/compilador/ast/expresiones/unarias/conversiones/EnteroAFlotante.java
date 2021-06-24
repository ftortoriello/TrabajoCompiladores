package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("Entero a flotante", expresion, Tipo.FLOAT);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion().evaluar();
        if (!(expr instanceof Literal)) {
            return expr;
        }

        return new Literal(((Literal) expr).getValorNumerico().floatValue(), Tipo.FLOAT);
    }
}
