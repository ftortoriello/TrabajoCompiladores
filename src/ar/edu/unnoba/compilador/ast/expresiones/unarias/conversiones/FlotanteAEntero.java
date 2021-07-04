package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Entero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Flotante;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

public class FlotanteAEntero extends OperacionConversion {
    public FlotanteAEntero(Expresion expresion) {
        super("Flotante a entero", expresion, Tipo.INTEGER);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion expr = getExpresion().evaluar();
        if (!(expr instanceof Flotante)) {
            return this;
        }

        double nroOriginal = ((Flotante) expr).getValor();
        return new Entero((int) Math.round(nroOriginal));
    }

    @Override
    public String getInstruccionIR() {
        return "fptosi";
    }
}
