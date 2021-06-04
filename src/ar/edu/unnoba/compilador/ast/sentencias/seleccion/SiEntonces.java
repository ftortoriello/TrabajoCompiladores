package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class SiEntonces extends Seleccion {
    private Bloque bloqueSiEntonces;

    public SiEntonces(String nombre, Expresion condicion, Bloque bloqueSiEntonces) {
        super(nombre, condicion);
        bloqueSiEntonces.setNombre("THEN");
        this.bloqueSiEntonces = bloqueSiEntonces;
    }

    public SiEntonces(Expresion condicion, Bloque bloqueSiEntonces) {
        super("Bloque\nIF-THEN", condicion);
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
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public SiEntonces accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
