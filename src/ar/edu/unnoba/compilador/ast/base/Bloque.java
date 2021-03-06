package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.util.ArrayList;
import java.util.List;

public class Bloque extends Sentencia {
    private List<Nodo> sentencias;
    private Alcance alcance;
    private final boolean esProgramaPrincipal;

    public Bloque(String nombre, List<Nodo> sentencias, boolean esProgramaPrincipal) {
        super(nombre);
        this.sentencias = sentencias;
        this.esProgramaPrincipal = esProgramaPrincipal;
    }

    public Bloque(String nombre, boolean esProgramaPrincipal) {
        this(nombre, new ArrayList<>(), esProgramaPrincipal);
    }

    public Bloque(String nombre, List<Nodo> sentencias) {
        this(nombre, sentencias, false);
    }

    public void setSentencias(List<Nodo> sentencias) {
        this.sentencias = sentencias;
    }

    public Bloque(String nombre, List<Nodo> sentencias, boolean esProgramaPrincipal, Alcance alcance) {
        this(nombre, sentencias, esProgramaPrincipal);
        this.alcance = alcance;
    }

    public Alcance getAlcance() {
        return alcance;
    }

    public void setAlcance(Alcance alcance) {
        this.alcance = alcance;
    }

    public boolean esProgramaPrincipal() {
        return esProgramaPrincipal;
    }

    public List<Nodo> getSentencias() {
        return sentencias;
    }

    @Override
    public String getEtiqueta() {
        return this.getNombre();
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Bloque accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
