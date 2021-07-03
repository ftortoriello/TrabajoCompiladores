package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;

/**
 * Clase que engloba a la declaración de variables, a los parámetros de funciones y la declaración
 * de funciones.
 */
public abstract class Declaracion extends Sentencia {
    private Identificador ident;

    public Declaracion(Identificador ident) {
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
    public String getNombre() {
        return ident.toString();
    }

    @Override
    public String getEtiqueta() {
        return getClass().getSimpleName();
    }
}
