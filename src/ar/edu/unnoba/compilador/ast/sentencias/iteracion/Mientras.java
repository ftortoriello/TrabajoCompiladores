package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Tiene un nodo "expresion" de tipo booleano (la condición) y un nodo "bloque"
 * de sentencias. */

public class Mientras extends Sentencia {
    private Expresion condicion;
    private Bloque bloqueSentencias;

    public Mientras(Expresion condicion, Bloque bloqueSentencias) {
        super("Bloque\nWHILE");
        this.condicion = condicion;
        bloqueSentencias.setNombre("Cuerpo\nWHILE");
        this.bloqueSentencias = bloqueSentencias;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public void setCondicion(Expresion condicion) {
        this.condicion = condicion;
    }

    public Bloque getBloqueSentencias() {
        return bloqueSentencias;
    }

    public void setBloqueSentencias(Bloque bloqueSentencias) {
        this.bloqueSentencias = bloqueSentencias;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Mientras accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
