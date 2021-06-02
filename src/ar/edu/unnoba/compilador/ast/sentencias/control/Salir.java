package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.Alcance;

public class Salir extends Control {
    public Salir() {
        super("BREAK");
    }

    public Salir(Alcance alcance) {
        super("BREAK", alcance);
    }
}
