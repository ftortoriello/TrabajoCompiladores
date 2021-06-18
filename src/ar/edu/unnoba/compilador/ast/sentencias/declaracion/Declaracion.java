package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;

public abstract class Declaracion extends Sentencia {
    // Clase que engloba a la declaración de variables, a los
    // parámetros de funciones y la declaración de funciones.

    private Identificador ident;

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

    public Tipo getTipo() {
        return ident.getTipo();
    }

    public void setTipo(Tipo tipo) {
        ident.setTipo(tipo);
    }

    @Override
    public String toString() {
        return ident.toString();
    }

    @Override
    public String getEtiqueta() {
        return getClass().getSimpleName();
    }
}
