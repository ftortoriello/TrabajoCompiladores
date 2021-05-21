package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.instrucciones.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.List;

public class Bloque extends Nodo {

    private Alcance alcance;
    private List<Sentencia> sentencias;
    private boolean esProgramaPrincipal = false;

    public Bloque(List<Sentencia> sentencias, String nombre) {
        super(nombre);
        this.sentencias = sentencias;
    }

    public Bloque(List<Sentencia> sentencias, String nombre, Alcance alcance) {
        super(nombre);
        this.alcance = alcance;
        this.sentencias = sentencias;
    }

    public Bloque(List<Sentencia> sentencias, String nombre, boolean esProgramaPrincipal) {
        super(nombre);
        this.esProgramaPrincipal = esProgramaPrincipal;
        this.sentencias = sentencias;
    }

    public Bloque(List<Sentencia> sentencias, String nombre, boolean esProgramaPrincipal, Alcance alcance) {
        super(nombre);
        this.alcance = alcance;
        this.esProgramaPrincipal = esProgramaPrincipal;
        this.sentencias = sentencias;
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

    public void setEsProgramaPrincipal(boolean esProgramaPrincipal) {
        this.esProgramaPrincipal = esProgramaPrincipal;
    }

    public List<Sentencia> getSentencias() {
        return sentencias;
    }

    public void setSentencias(List<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Bloque accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
