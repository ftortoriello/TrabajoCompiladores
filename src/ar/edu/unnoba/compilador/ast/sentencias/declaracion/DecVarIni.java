package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class DecVarIni extends DecVar {
    // Clase para las variables que hayan sido inicializadas al momento de crearse.

    private Expresion expresion;

    public DecVarIni(String nombre, Identificador ident, Expresion expr) {
        super(nombre, ident);
        this.expresion = expr;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionDeAlcance {
        v.visit(this);
    }

    @Override
    public DecVarIni accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}