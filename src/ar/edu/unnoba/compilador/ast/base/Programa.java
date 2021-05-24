package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class Programa extends Nodo {

    private List<Sentencia> encabezado;
    private Bloque cuerpo;

    public Programa(String nombre, List<Sentencia> encabezado, Bloque cuerpo) {
        super(nombre);
        this.cuerpo = cuerpo;
        this.encabezado = encabezado;
    }

    public Programa(String nombre, Bloque cuerpo) {
        super(nombre);
        this.cuerpo = cuerpo;
        this.encabezado = Collections.emptyList();
    }

    public List<Sentencia> getEncabezado() {
        return encabezado;
    }

    public Bloque getCuerpo() {
        return this.cuerpo;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(%s, %s)",
                this.getClass().getSimpleName(), this.getEncabezado(),
                this.getCuerpo()));
    }

    @Override
    public String toString() {
        return this.getEtiqueta();
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        // TODO: Sin implementar
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: Sin implementar
        return null;
    }
}
