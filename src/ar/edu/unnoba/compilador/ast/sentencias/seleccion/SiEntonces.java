package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class SiEntonces extends Seleccion {
    private Bloque bloqueSiEntonces;

    public SiEntonces(String nombre, Expresion condicion, Bloque bloqueSiEntonces) {
        super(nombre, condicion);
        bloqueSiEntonces.setNombre("THEN");
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    public SiEntonces(Expresion condicion, Bloque bloqueSiEntonces) {
        super("Bloque\\nIF-THEN", condicion);
        bloqueSiEntonces.setNombre("THEN");
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    public Bloque getBloqueSiEntonces() {
        return bloqueSiEntonces;
    }

    public void setBloqueSiEntonces(Bloque bloqueSiEntonces) {
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public SiEntonces accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
