package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class SiEntoncesSino extends SiEntonces {
    Bloque bloqueSino;

    public SiEntoncesSino(Expresion condicion, Bloque bloqueSiEntonces, Bloque bloqueSino) {
        super("Bloque\nIF-THEN-ELSE", condicion, bloqueSiEntonces);
        bloqueSino.setNombre("ELSE");
        this.bloqueSino = bloqueSino;
    }

    public Bloque getBloqueSino() {
        return bloqueSino;
    }

    public void setBloqueSino(Bloque bloqueSino) {
        this.bloqueSino = bloqueSino;
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public SiEntoncesSino accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
