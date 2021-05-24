package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;

public abstract class Control extends Sentencia {
    private Alcance alcance;

    public Control(String nombre) {
        super(nombre);
    }

    public Control(String nombre, Alcance alcance) {
        super(nombre);
        this.alcance = alcance;
    }

    public Alcance getAlcance() {
        return alcance;
    }

    public void setAlcance(Alcance alcance) {
        this.alcance = alcance;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(%s)",
                this.getClass().getSimpleName(), this.getNombre()));
    }
}

