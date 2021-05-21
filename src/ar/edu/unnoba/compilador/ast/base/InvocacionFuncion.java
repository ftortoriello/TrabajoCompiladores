package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.Collections;
import java.util.List;

public class InvocacionFuncion extends Expresion {
    List<Expresion> argumentos;

    public InvocacionFuncion(String nombre, List<Expresion> argumentos, Tipo tipo) {
        super(tipo, nombre);
        this.argumentos = argumentos;
    }

    public InvocacionFuncion(String nombre, List<Expresion> argumentos) {
        super(nombre);
        this.argumentos = argumentos;
    }

    public InvocacionFuncion(String nombre, Tipo tipo) {
        super(tipo, nombre);
        this.argumentos = Collections.emptyList();
    }

    public InvocacionFuncion(String nombre) {
        super(nombre);
        this.argumentos = Collections.emptyList();
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("InvocacionFuncion[%s](%s, %s)",
                this.getTipo(), this.getNombre(), this.argumentos.toString()));
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
