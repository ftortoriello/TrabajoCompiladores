package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Declaración de variable o parámetro sin inicializar. */
public class DecVar extends Declaracion {
    public DecVar(String nombre, Identificador ident) {
        super(nombre, ident);
    }

    @Override
    public String getEtiqueta() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getIdent().getNombre();
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public DecVar accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
