package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Tiene un nodo "expresion" de tipo booleano (la condición) y un nodo "bloque"
 * de sentencias. */
// TODO: error de compatibilidad si la expresión no puede evaluarse como BOOLEAN

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
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Mientras accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
