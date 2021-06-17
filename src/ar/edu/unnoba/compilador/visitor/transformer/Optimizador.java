package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

/* Transformer que aplica Constant Folding.
 */

public class Optimizador extends Transformer {

    @Override
    public Expresion transform(OperacionBinaria ob) throws ExcepcionDeTipos {
        ob = (OperacionBinaria) super.transform(ob);
        return ob.evaluar();
    }

    @Override
    public Expresion transform(Relacion r) throws ExcepcionDeTipos {
        r = (Relacion) super.transform(r);
        return r.evaluar();
    }

    @Override
    public Expresion transform(OperacionUnaria ou) throws ExcepcionDeTipos {
        ou = (OperacionUnaria) super.transform(ou);
        return ou.evaluar();
    }
}
