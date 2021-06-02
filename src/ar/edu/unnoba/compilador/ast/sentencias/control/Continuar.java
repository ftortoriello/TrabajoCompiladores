package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.Alcance;

public class Continuar extends Control {
    public Continuar() {
        super("CONTINUE");
    }

    public Continuar(Alcance alcance) {
        super("CONTINUE", alcance);
    }
}
