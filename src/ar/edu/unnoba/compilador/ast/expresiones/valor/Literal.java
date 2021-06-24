package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public class Literal extends Valor {
    private final String valor;

    public Literal(String valor, Tipo tipo, String nombre) {
        super(tipo, nombre);
        this.valor = valor;
    }

    public Literal(String valor, Tipo tipo) {
        super(tipo, "Literal " + tipo);
        this.valor = valor;
    }

    public Literal(Number numero, Tipo tipo, String nombre) {
        super(tipo, nombre);
        switch (tipo) {
            case INTEGER:
                this.valor = Integer.toString(numero.intValue());
                break;
            case FLOAT:
                this.valor = Float.toString(numero.floatValue());
                break;
            default: throw new IllegalStateException(
                    String.format("Tipo inesperado: %s. Se esperaba un tipo numérico.", tipo));
        }
    }

    public Literal(Number numero, Tipo tipo) {
        this(numero, tipo, "Literal " + tipo);
    }

    public Literal(boolean valor, Tipo tipo, String nombre) {
        super(tipo, nombre);
        if (!tipo.equals(Tipo.BOOLEAN)) throw new IllegalStateException(
                String.format("%s: Tipo inesperado: %s. Se esperaba BOOLEAN.", this, tipo));
        this.valor = Boolean.toString(valor);
    }

    public Literal(boolean valor, Tipo tipo) {
        this(valor, tipo, "Literal " + tipo);
    }

    public String getValor() {
        return valor;
    }

    public Number getValorNumerico() {
        switch (getTipo()) {
            case FLOAT:   return Float.parseFloat(valor);
            case INTEGER: return Integer.parseInt(valor);
            default: throw new IllegalStateException(
                    String.format("%s no es de tipo numérico.", this));
        }
    }

    public boolean getValorBooleano() {
        if (getTipo().equals(Tipo.BOOLEAN))
            return Boolean.parseBoolean(valor);
        else
            throw new IllegalStateException(String.format("%s no es de tipo booleano.", this));
    }

    public String getValorIR() {
        switch (getTipo()) {
            //case BOOLEAN: return getValorBooleano() ? "1" : "0";
            case BOOLEAN: return getValor(); // usar las constantes de IR "true" y "false"

            case FLOAT:
                // Agregar ceros si faltan
                String v = valor;
                if (v.startsWith(".")) v = "0" + v;
                if (v.endsWith(".")) v = v + "0";
                return v;

            case INTEGER: return valor;

            default: throw new IllegalStateException(
                    String.format("%s no es de tipo numérico.", this));
        }
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\\n<%s>", getValor(), getTipo());
    }

    @Override
    public String toString() {
        return getValor();
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
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
}
