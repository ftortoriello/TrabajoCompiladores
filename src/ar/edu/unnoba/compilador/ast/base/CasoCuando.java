package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class CasoCuando extends Nodo {

    OperadorCmp op;
    Expresion expr;
    Bloque bloque;

    public CasoCuando(OperadorCmp op, Expresion expr, Bloque bloque) {
        super("CasoCuando");
        this.op = op;
        this.expr = expr;
        this.bloque = bloque;
    }

    public OperadorCmp getOp() {
        return op;
    }

    public void setOp(OperadorCmp op) {
        this.op = op;
    }

    public Expresion getExpr() {
        return expr;
    }

    public void setExpr(Expresion expr) {
        this.expr = expr;
    }

    public Bloque getBloque() {
        return bloque;
    }

    public void setBloque(Bloque bloque) {
        this.bloque = bloque;
    }

    @Override
    public String getEtiqueta() {
        return String.format(String.format("%s(%s, %s, %s)",
                this.getClass().getSimpleName(), this.getOp(),
                this.getExpr().getEtiqueta(), this.getBloque().getEtiqueta()));
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
