package ar.edu.unnoba.compilador.ast.sentencias.control;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Retorno extends Control {
    // TODO: Tendría que tener un atributo tipo Alcance?
    Expresion expr;

    public Retorno (Expresion expr) {
        super("Sentencia RETURN");
        this.expr = expr;
    }

    public Retorno (Expresion expr, Alcance alcance) {
        super("Sentencia RETURN", alcance);
        this.expr = expr;
    }

    public Expresion getExpr() {
        return expr;
    }

    public void setExpr(Expresion expr) {
        this.expr = expr;
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
