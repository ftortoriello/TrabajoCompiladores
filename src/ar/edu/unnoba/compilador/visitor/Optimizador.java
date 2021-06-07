package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Division;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Multiplicacion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Resta;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Conjuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Disyuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;

/* Transformer que realiza distintas optimizaciones antes de generar el código IR,
 * como convertir estructuras a una equivalente y aplicar constant folding.
 */
// TODO: implementar conversor de estructuras

public class Optimizador extends Transformer {

    @Override
    public Para transform(Para p) throws ExcepcionDeTipos {
        p = super.transform(p);
        return p;
    }

    @Override
    public Cuando transform(Cuando c) throws ExcepcionDeTipos {
        c = super.transform(c);
        return c;
    }

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
