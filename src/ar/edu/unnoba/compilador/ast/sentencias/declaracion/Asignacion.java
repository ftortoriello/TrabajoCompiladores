package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Asignacion extends Declaracion {
    private Expresion expresion;

    public Asignacion(Identificador ident, Expresion e){
        super("=", ident);
        this.expresion = e;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

    public Expresion getExpresion(){
        return expresion;
    }

    @Override
    public String getEtiqueta() {
        return getNombre();
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Asignacion accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
