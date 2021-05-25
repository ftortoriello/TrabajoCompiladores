package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Mientras extends Sentencia {
    // Tiene un nodo "expresion" de tipo booleano (la condición) y un nodo "bloque" de sentencias.
    // TODO: ¿y si la expresión no puede evaluarse como BOOL?

    Expresion condicion;
    Bloque bloqueSentencias;

    public Mientras(Expresion condicion, Bloque bloqueSentencias) {
        super("Bloque WHILE");
        this.condicion = condicion;
        this.bloqueSentencias = bloqueSentencias;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public Bloque getBloqueSentencias() {
        return bloqueSentencias;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(COND.: %s)",
                this.getClass().getSimpleName(), this.getCondicion().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
