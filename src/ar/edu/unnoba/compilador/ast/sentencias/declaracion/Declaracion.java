package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;

public abstract class Declaracion extends Sentencia {

    Identificador ident;

    public Declaracion(String nombre, Identificador ident) {
        super(nombre);
        this.ident = ident;
    }

    public Identificador getIdent() {
        return ident;
    }

    public void setIdent(Identificador ident) {
        this.ident = ident;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s", this.getClass().getSimpleName());
    }
}
