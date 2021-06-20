package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("Entero a flotante", expresion, Tipo.FLOAT);
    }

    public Expresion evaluar() throws ExcepcionTransformer {
        if (!(getExpresion() instanceof Literal)) {
            // TODO si no es literal se rompe
            return this;
        }

        String nroOriginal = ((Literal)getExpresion()).getValor();
        Number nroConvertido = Math.round(Float.parseFloat(nroOriginal));
        return new Literal(nroConvertido, Tipo.FLOAT);
    }
}
