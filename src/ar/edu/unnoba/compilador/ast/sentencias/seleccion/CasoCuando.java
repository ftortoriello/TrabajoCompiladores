package ar.edu.unnoba.compilador.ast.sentencias.seleccion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.OperadorCmp;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class CasoCuando extends Sentencia {
    private OperadorCmp op;
    private Expresion expr;
    private Bloque bloque;

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

    public Expresion getExpresion() {
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
        return OperadorCmp.getStrOp(op);
    }

    @Override
    public void accept(Visitor v) throws ExcepcionDeAlcance {
        v.visit(this);
    }

    @Override
    public Sentencia accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
