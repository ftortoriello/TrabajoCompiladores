package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Literal extends Valor {
    private final String valor;

    public Literal(String valor, Tipo tipo, String nombre) {
        super(tipo, nombre);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\n<%s>", getValor(), getTipo());
    }

    @Override
    public String toString() {
        return getValor();
    }

    @Override
    public void accept(Visitor v) throws ExcepcionDeAlcance {
        v.visit(this);
    }

    @Override
    public Literal accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    @Override
    public Expresion evaluar() {
        return this;
    }
}
