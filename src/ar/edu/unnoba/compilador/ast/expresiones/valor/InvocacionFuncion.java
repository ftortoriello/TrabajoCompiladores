package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.List;

public class InvocacionFuncion<A> extends Valor {
    // Pongo tipo genérico porque los argumentos pueden ser expresiones o strings
    // TODO: acomodar llamadas, si acepta genéricos tendría que pasarse el tipo
    final List<A> argumentos;

    public InvocacionFuncion(String nombre, List<A> argumentos, Tipo tipo) {
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
    public InvocacionFuncion accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}