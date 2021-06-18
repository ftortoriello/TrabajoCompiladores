package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Param extends Declaracion {
    // Clase para los parámetros de las funciones que no tengan valor por defecto.
    
    public Param(String nombre, Identificador ident) {
        super(nombre, ident);
    }

    @Override
    public void accept(Visitor v) throws ExcepcionDeAlcance {
        v.visit(this);
    }

    @Override
    public Param accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
