package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class SiEntoncesSino extends SiEntonces {

    Bloque bloqueSino;

    public SiEntoncesSino(Expresion condicion, Bloque bloqueSiEntonces, Bloque bloqueSino) {
        super("Bloque IF-THEN-ELSE", condicion, bloqueSiEntonces);
        this.bloqueSino = bloqueSino;
    }

    public Bloque getBloqueSino() {
        return bloqueSino;
    }

    public void setBloqueSino(Bloque bloqueSino) {
        this.bloqueSino = bloqueSino;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(%s, %s)",
                this.getClass().getSimpleName(), this.getCondicion().getEtiqueta(),
                this.getBloqueSiEntonces().getEtiqueta(), this.getBloqueSino().getEtiqueta()));
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
