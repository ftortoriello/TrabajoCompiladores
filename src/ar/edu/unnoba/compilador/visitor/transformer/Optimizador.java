package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;

/* Transformer que aplica Constant Folding. */

// FIXME: Me parece que acá elimina conversores que no tendría que eliminar...

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
