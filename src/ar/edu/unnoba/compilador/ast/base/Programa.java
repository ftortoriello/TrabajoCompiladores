package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;


public class Programa extends Nodo {

    private Encabezado encabezado;
    private Bloque cuerpo;

    public Programa(String nombre, Encabezado encabezado, Bloque cuerpo) {
        super(nombre);
        this.cuerpo = cuerpo;
        this.encabezado = encabezado;
    }

    public Encabezado getEncabezado() {
        return this.encabezado;
    }

    public Bloque getCuerpo() {
        return this.cuerpo;
    }

    @Override
    public String toString() {
        return this.getEtiqueta();
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: Sin implementar
        return null;
    }
}
