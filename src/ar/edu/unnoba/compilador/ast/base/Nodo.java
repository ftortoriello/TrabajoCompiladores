package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.sintaxis.Util;
import ar.edu.unnoba.compilador.util.Constantes;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import java_cup.runtime.ComplexSymbolFactory;

public abstract class Nodo {
    private String nombre;

    /** Posición en que comienza el token en el archivo de entrada. */
    private ComplexSymbolFactory.Location posIzq;

    /** Posición en que termina el token en el archivo de entrada. */
    private ComplexSymbolFactory.Location posDer;

    public Nodo() {
        if (Constantes.mostrarCreacionNodos) {
            System.out.println("Se creó el nodo " + this.getClass().getSimpleName());
        }
    }

    public Nodo(String nombre) {
        this();
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPosicion() {
        return Util.getPosicion(posIzq, posDer);
    }

    public void setPosicion(ComplexSymbolFactory.Location izq, ComplexSymbolFactory.Location der) {
        this.posIzq = izq;
        this.posDer = der;
    }

    @Override
    public String toString() {
        if (nombre == null) {
            return super.toString();
        } else {
            return nombre;
        }
    }

    public String getEtiqueta() {
        if (nombre == null) {
            return this.getClass().getSimpleName();
        } else {
            return nombre;
        }
    }

    public abstract void accept(Visitor v) throws ExcepcionVisitor;

    public abstract Nodo accept(Transformer t) throws ExcepcionTransformer;
}
