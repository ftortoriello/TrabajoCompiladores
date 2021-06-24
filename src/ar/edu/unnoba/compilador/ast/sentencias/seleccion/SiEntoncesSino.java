package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class SiEntoncesSino extends SiEntonces {
    private Bloque bloqueSino;

    public SiEntoncesSino(Expresion condicion, Bloque bloqueSiEntonces, Bloque bloqueSino) {
        super("Bloque\\nIF-THEN-ELSE", condicion, bloqueSiEntonces);
        bloqueSino.setNombre("ELSE");
        this.bloqueSino = bloqueSino;
    }

    // TODO: Necesito este constructor con el bloque else vacío para la conversión de switch a if,
    // pero capaz pueda hacerse de otra manera (¿sino para qué tenemos la clase SiEntonces?)
    public SiEntoncesSino(Expresion condicion, Bloque bloqueSiEntonces) {
        super("Bloque\\nIF-THEN-ELSE", condicion, bloqueSiEntonces);
        this.bloqueSino = null;
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
