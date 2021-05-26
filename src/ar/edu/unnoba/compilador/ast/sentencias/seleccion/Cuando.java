package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.CasoCuando;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cuando extends Seleccion {

    List<CasoCuando> bloqueCuando;
    Bloque bloqueElse;

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando) {
        super("Bloque WHEN", expr);
        this.bloqueCuando = bloqueCuando;
        this.bloqueElse = new Bloque("Bloque vacío", false);
    }

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando, Bloque bloqueElse) {
        super("Bloque WHEN-ELSE", expr);
        this.bloqueCuando = bloqueCuando;
        this.bloqueElse = bloqueElse;
    }

    public List<CasoCuando> getCasos() {
        return bloqueCuando;
    }

    public void setCasos(List<CasoCuando> bloqueCuando) {
        this.bloqueCuando = bloqueCuando;
    }

    public Bloque getBloqueElse() {
        return bloqueElse;
    }

    public void setBloqueElse(Bloque bloqueElse) {
        this.bloqueElse = bloqueElse;
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
