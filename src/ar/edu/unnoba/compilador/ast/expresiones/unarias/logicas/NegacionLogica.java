package ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class NegacionLogica extends OperacionUnaria {
    public NegacionLogica(Expresion expresion) {
        super("NOT", expresion);
    }

    @Override
    public String getRefIR() {
        // Como implementamos cortocircuito booleano, no le generamos una referencia a los NOT,
        // invertimos las etiquetas de salto.
        // Retornar la referencia de la expresión.
        return getExpresion().getRefIR();
    }

    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion();
        if (expr instanceof NegacionLogica) {
            // cancelar "not not"
            return ((NegacionLogica) expr).getExpresion();
        }

        if (!(expr instanceof Literal)) {
            return this;
        }

        boolean valorNegado = !((Literal) getExpresion()).getValorBooleano();
        return new Literal(String.valueOf(valorNegado), Tipo.BOOLEAN);
    }
}
