package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public class Bloque extends Nodo {
    private List<Nodo> sentencias;
    private Alcance alcance;
    private final boolean esProgramaPrincipal;

    public Bloque(String nombre, List<Nodo> sentencias, boolean esProgramaPrincipal) {
        super(nombre);
        this.sentencias = sentencias;
        this.esProgramaPrincipal = esProgramaPrincipal;
    }

    public Bloque(String nombre, boolean esProgramaPrincipal) {
        super(nombre);
        this.sentencias = new ArrayList<>();
        this.esProgramaPrincipal = esProgramaPrincipal;
    }

    public Boolean esCompuesto() {
        return this.sentencias.size() > 1;
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
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Bloque accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
