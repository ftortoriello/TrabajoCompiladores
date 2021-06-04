package ar.edu.unnoba.compilador.token;

import ar.edu.unnoba.compilador.sintaxis.ParserSym;

import java.util.Arrays;

public class Token extends java_cup.runtime.Symbol {
    public final String nombre;
    public final int linea;
    public final int columna;
    public final Object valor;

    public Token(String nombre) {
        this(nombre, null);
    }

    public Token(String nombre, Object valor) {
        this(nombre, -1, -1, valor);
    }

    public Token(String nombre, int linea, int columna) {
        this(nombre, linea, columna, null);
    }

    public Token(String nombre, int linea, int columna, Object valor) {
        super(Arrays.asList(ParserSym.terminalNames).indexOf(nombre), linea, columna, valor);
        this.nombre = nombre;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    public String getPosicion() {
        if (linea > -1 && columna > -1)
            return String.format("(L:%d, C:%d)", linea + 1, columna + 1);
        else
            return null;
    }

    @Override
    public String toString() {
        String posicion = getPosicion();
        posicion = (posicion == null ? "" : " @ " + posicion);
        if (valor == null)
            return "[" + this.nombre + "]" + posicion;
        else
            return "[" + this.nombre + "] -> (" + this.valor + ")" + posicion;
    }
}
