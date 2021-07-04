package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.util.Collections;
import java.util.List;

public class Cuando extends Seleccion {
    private List<CasoCuando> bloqueCuando;
    private Bloque bloqueElse;

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando) {
        super("Bloque\\nWHEN", expr);
        Collections.reverse(bloqueCuando);
        this.bloqueCuando = bloqueCuando;
        this.bloqueElse = new Bloque("Bloque vacío", false);
    }

    public Cuando(Expresion expr, List<CasoCuando> bloqueCuando, Bloque bloqueElse) {
        super("Bloque\\nWHEN-ELSE", expr);
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
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Nodo accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
