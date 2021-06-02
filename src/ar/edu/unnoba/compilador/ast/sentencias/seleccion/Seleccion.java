package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;

public abstract class Seleccion extends Sentencia {
    Expresion condicion;

    public Seleccion(String nombre, Expresion condicion) {
        super(nombre);
        this.condicion = condicion;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public void setCondicion(Expresion condicion) {
        this.condicion = condicion;
    }
}
