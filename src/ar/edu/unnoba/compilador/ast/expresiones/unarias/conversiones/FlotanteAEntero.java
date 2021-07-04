package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

public class FlotanteAEntero extends OperacionConversion {
    public FlotanteAEntero(Expresion expresion) {
        super("Flotante a entero", expresion, Tipo.INTEGER);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion().evaluar();
        if (!(expr instanceof Literal)) {
            return this;
        }

        double nroOriginal = ((Literal) expr).getValorNumerico().doubleValue();
        Number nroConvertido = Math.round(nroOriginal);
        return new Literal(nroConvertido, Tipo.INTEGER);
    }

    @Override
    public String getInstruccionIR() {
        return "fptosi";
    }
}
