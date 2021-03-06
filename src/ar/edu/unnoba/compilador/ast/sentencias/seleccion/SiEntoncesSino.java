package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class SiEntoncesSino extends SiEntonces {
    private Bloque bloqueSino;

    public SiEntoncesSino(Expresion condicion, Bloque bloqueSiEntonces, Bloque bloqueSino) {
        super("Bloque\\nIF-THEN-ELSE", condicion, bloqueSiEntonces);
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
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public SiEntoncesSino accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
