package ar.edu.unnoba.compilador.ast.operaciones.binarias;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Tipo;

public abstract class OperacionBinaria extends Expresion {
    private Expresion izquierda;
    private Expresion derecha;


    public OperacionBinaria(Expresion izquierda, Expresion derecha) {
        super(Tipo.UNKNOWN);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, String nombre) {
        super(Tipo.UNKNOWN, nombre);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, Tipo tipo) {
        super(tipo);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public OperacionBinaria(Expresion izquierda, Expresion derecha, Tipo tipo, String nombre) {
        super(tipo, nombre);
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public void setIzquierda(Expresion izquierda) {
        this.izquierda = izquierda;
    }

    public void setDerecha(Expresion derecha) {
        this.derecha = derecha;
    }

    public Expresion getIzquierda(){
        return izquierda;
    }

    public Expresion getDerecha(){
        return derecha;
    }

    @Override
    public String getEtiqueta() {
        if(this.getTipo() != null){
            return String.format("%s %s", this.getNombre(), this.getTipo());
        }
        return String.format("%s", this.getNombre());
    }

}
