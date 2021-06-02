package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;

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
}
