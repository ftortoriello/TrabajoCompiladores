package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.Transformer;

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
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Literal accept_transfomer(Transformer t) {
        return t.transform(this);
    }
}
