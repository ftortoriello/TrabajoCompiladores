package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class Continuar extends Control {
    public Continuar() {
        super("CONTINUE");
    }

    public Continuar(Alcance alcance) {
        super("CONTINUE", alcance);
    }
}
