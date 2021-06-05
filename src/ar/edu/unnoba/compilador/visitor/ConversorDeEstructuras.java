package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;

/* Transformer que convierte estructuras for a while, y when a if. */
// TODO: implementar conversor de estructuras

public class ConversorDeEstructuras extends Transformer {
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
}
