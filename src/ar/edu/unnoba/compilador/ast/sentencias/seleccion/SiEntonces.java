package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class SiEntonces extends Seleccion {

    Bloque bloqueSiEntonces;

    public SiEntonces(String nombre, Expresion condicion, Bloque bloqueSiEntonces) {
        super("Bloque IF-THEN", condicion);
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    public SiEntonces(Expresion condicion, Bloque bloqueSiEntonces) {
        super("Bloque IF-THEN", condicion);
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    public Bloque getBloqueSiEntonces() {
        return bloqueSiEntonces;
    }

    public void setBloqueSiEntonces(Bloque bloqueSiEntonces) {
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(%s, %s)",
                this.getClass().getSimpleName(), this.getCondicion().getEtiqueta(),
                this.getBloqueSiEntonces().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
