package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class Cuando extends Seleccion {
    private List<CasoCuando> bloqueCuando;
    private Bloque bloqueElse;

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando) {
        super("Bloque\nWHEN", expr);
        Collections.reverse(bloqueCuando);
        this.bloqueCuando = bloqueCuando;
        this.bloqueElse = new Bloque("Bloque vacío", false);
    }

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando, Bloque bloqueElse) {
        super("Bloque\nWHEN-ELSE", expr);
        Collections.reverse(bloqueCuando);
        this.bloqueCuando = bloqueCuando;
        bloqueElse.setNombre("ELSE");
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
    public Nodo accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
