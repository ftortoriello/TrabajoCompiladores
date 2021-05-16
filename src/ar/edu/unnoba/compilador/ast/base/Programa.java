package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Programa extends Nodo {
    // TODO: dejar como estaba, lo modifiqué para que no pase del nodo raíz
    private String cuerpo;

    public Programa(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }


    /*
    private Bloque cuerpo;

    public Programa(Bloque cuerpo) {
        this.cuerpo = cuerpo;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(Bloque cuerpo) {
        this.cuerpo = cuerpo;
    }
    */

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        // TODO: Sin implementar
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: Sin implementar
        return null;
    }
}
