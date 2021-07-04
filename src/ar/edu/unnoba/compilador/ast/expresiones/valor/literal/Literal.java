package ar.edu.unnoba.compilador.ast.expresiones.valor.literal;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Valor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class Literal extends Valor {
    public Literal(Tipo tipo) {
        super(tipo);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\\n<%s>", this, getTipo());
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public Literal accept(Transformer t) {
        return t.transform(this);
    }

    @Override
    public Expresion evaluar() {
        return this;
    }

    // *** Funciones auxiliares para literales numéricos usados por el constant folding ***

    public static Number getNumero(Expresion expr) {
        if (expr instanceof Flotante) {
            return ((Flotante) expr).getValor();
        } else if (expr instanceof Entero) {
            return ((Entero) expr).getValor();
        } else {
            throw new IllegalStateException("Tipo de expresión inesperado: " + expr);
        }
    }

    public static Literal crearNumero(Number valor, Tipo tipo) {
        switch (tipo) {
            case INTEGER: return new Entero(valor.intValue());
            case FLOAT:   return new Flotante(valor.doubleValue());
            default:      throw new IllegalStateException("Tipo de literal inesperado: " + tipo);
        }
    }
}
