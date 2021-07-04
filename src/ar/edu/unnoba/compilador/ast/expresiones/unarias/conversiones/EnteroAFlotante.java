package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Entero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Flotante;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

public class EnteroAFlotante extends OperacionConversion {
    public EnteroAFlotante(Expresion expresion) {
        super("Entero a flotante", expresion, Tipo.FLOAT);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion().evaluar();
        if (!(expr instanceof Entero)) {
            return this;
        }

        return new Flotante(((Entero) expr).getValor().doubleValue());
    }

    @Override
    public String getInstruccionIR() {
        return "sitofp";
    }
}
