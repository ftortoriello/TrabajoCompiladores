package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class DecFuncion extends Declaracion {

    List<DecVar> args;
    Bloque bloque;

    public DecFuncion(Identificador id, List<DecVar> args, Bloque bloque) {
        super("Declaración de función con argumentos", id);
        // Invierto el orden de los argumentos porque debido a la forma de las reglas los lee al revés
        Collections.reverse(args);
        this.args = args;
        this.bloque = bloque;
    }

    public DecFuncion(Identificador id, Bloque bloque) {
        super("Declaración de función sin argumentos", id);
        this.args = Collections.emptyList();
        this.bloque = bloque;
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
        return String.format("%s %s", getClass().getSimpleName(), getIdent().getEtiqueta());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        // TODO: implementar
        return null;
    }
}
