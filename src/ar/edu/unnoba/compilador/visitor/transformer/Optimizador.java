package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

/* Transformer que aplica Constant Folding. */

public class Optimizador extends Transformer {
    @Override
    public Expresion transform(OperacionBinaria ob) throws ExcepcionTransformer {
        return super.transform(ob).evaluar();
    }

    @Override
    public Expresion transform(Relacion r) throws ExcepcionTransformer {
        return super.transform(r).evaluar();
    }

    @Override
    public Expresion transform(OperacionUnaria ou) throws ExcepcionTransformer {
        return super.transform(ou).evaluar();
    }
}
