package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.Transformer;

public class Identificador extends Valor {
    // TODO: Catalogar cosas que no sabemos si son variables, funciones o qué
    // TODO: Estos nodos tienen que quedar eliminados del AST cuando se defina qué son

    public Identificador(String nombre){
        super(Tipo.UNKNOWN, nombre);
    }

    public Identificador(String nombre, Tipo tipo){
        super(tipo, nombre);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s %s", getTipo(), getNombre());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Identificador accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
