package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class ParamDef extends Param {
    // Clase para los parámetros de funciones que tengan un valor por defecto

    private Expresion expresion;

    public ParamDef(String nombre, Identificador ident, Expresion expr) {
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
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Param accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
