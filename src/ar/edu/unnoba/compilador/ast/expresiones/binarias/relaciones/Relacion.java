package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.OperadorCmp;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Booleano;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class Relacion extends OperacionBinaria {

    public Relacion(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion izquierda = getIzquierda().evaluar();
        Expresion derecha = getDerecha().evaluar();
        if (!(izquierda instanceof Literal && derecha instanceof Literal)) {
            return this;
        }

        boolean resultado;

        if (izquierda.getTipo().equals(Tipo.BOOLEAN) && derecha.getTipo().equals(Tipo.BOOLEAN)) {
            boolean valorIzq = ((Booleano) izquierda).getValor();
            boolean valorDer = ((Booleano) derecha).getValor();
            resultado = calcularResultado(valorIzq, valorDer);
        } else {
            double valorIzq = Literal.getNumero(izquierda).doubleValue();
            double valorDer = Literal.getNumero(derecha).doubleValue();
            resultado = calcularResultado(valorIzq, valorDer);
        }

        return new Booleano(resultado);
    }

    protected abstract boolean calcularResultado(double izq, double der);

    protected abstract boolean calcularResultado(boolean izq, boolean der);

    /**
     * Crear una clase según sea el operador utilizado.
     * Si se utiliza el operador de IGUALDAD, devuelvo una clase Igualdad, etc.
     */
    public static Relacion getClaseRel(OperadorCmp op, Expresion expr1, Expresion expr2) {
        return switch (op) {
            case IGUALDAD    -> new Igualdad(expr1, expr2);
            case DESIGUALDAD -> new Desigualdad(expr1, expr2);
            case MAYOR       -> new Mayor(expr1, expr2);
            case MAYOR_IGUAL -> new MayorIgual(expr1, expr2);
            case MENOR       -> new Menor(expr1, expr2);
            case MENOR_IGUAL -> new MenorIgual(expr1, expr2);
            default -> throw new IllegalStateException("Operador inesperado: " + op);
        };
    }
}
