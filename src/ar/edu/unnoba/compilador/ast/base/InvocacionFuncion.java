package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class InvocacionFuncion<T> extends Expresion {
    // Pongo tipo genérico porque los argumentos pueden ser expresiones o strings
    List<T> argumentos;

    public InvocacionFuncion(String nombre, List<T> argumentos, Tipo tipo) {
        super(tipo, nombre);
        this.argumentos = argumentos;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s(%s)", getNombre(), this.argumentos.toString().replace("[", "").replace("]", ""));
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
