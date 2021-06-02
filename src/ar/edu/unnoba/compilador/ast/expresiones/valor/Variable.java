package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Clase para generar los alcances y validar los tipos.
 * TODO: Luego mediante un Transformer Variable reemplaza a Identificador
 */
public class Variable extends Valor {
    private DecVar declaracion;

    public Variable(DecVar declaracion) {
        this.declaracion = declaracion;
    }

    public DecVar getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecVar declaracion) {
        this.declaracion = declaracion;
    }

    @Override
    public <T> T accept(Visitor<T> v) {
        return v.visit(this);
    }

    @Override
    public Variable accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
