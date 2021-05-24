package ar.edu.unnoba.compilador.ast.sentencias;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class DecFuncion extends Sentencia {

    Identificador id;
    List<DecVar> args;
    Bloque bloque;

    public DecFuncion(Identificador id, List<DecVar> args, Bloque bloque) {
        super("Declaración de función sin argumentos");
        this.id = id;
        this.args = args;
        this.bloque = bloque;
    }

    public DecFuncion(Identificador id, Bloque bloque) {
        super("Declaración de función con argumentos");
        this.id = id;
        this.args = Collections.emptyList();
        this.bloque = bloque;
    }

    public Identificador getId() {
        return id;
    }

    public List<DecVar> getArgs() {
        return args;
    }

    public Bloque getBloque() {
        return bloque;
    }

    public void setBloque(Bloque bloque) {
        this.bloque = bloque;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s[%s](%s)",
                this.getClass().getSimpleName(), this.getId().getTipo(),
                this.getBloque().getEtiqueta()));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        // TODO: implementar
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: implementar
        return null;
    }
}
