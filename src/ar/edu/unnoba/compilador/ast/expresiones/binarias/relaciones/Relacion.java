package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.OperadorCmp;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
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
        if (!(izquierda instanceof Literal) || !(derecha instanceof Literal)) {
            return this;
        }

        boolean resultado;
        if (getTipo().equals(Tipo.BOOLEAN)) {
            boolean valorIzq = ((Literal) izquierda).getValorBooleano();
            boolean valorDer = ((Literal) derecha).getValorBooleano();
            resultado = calcularResultado(valorIzq, valorDer);
        } else {
            float valorIzq = ((Literal) izquierda).getValorNumerico().floatValue();
            float valorDer = ((Literal) derecha).getValorNumerico().floatValue();
            resultado = calcularResultado(valorIzq, valorDer);
        }

        return new Literal(String.valueOf(resultado), Tipo.BOOLEAN);
    }

    protected abstract boolean calcularResultado(float izq, float der);

    protected abstract boolean calcularResultado(boolean izq, boolean der);

    // Crea una clase según sea el operador utilizado
    // Si se utiliza el operador de IGUALDAD, devuelvo una clase Igualdad, etc.
    public static Relacion getClaseRel(OperadorCmp op, Expresion expr1, Expresion expr2) {
        switch (op) {
            case IGUALDAD:    return new Igualdad(expr1, expr2);
            case DESIGUALDAD: return new Desigualdad(expr1, expr2);
            case MAYOR:       return new Mayor(expr1, expr2);
            case MAYOR_IGUAL: return new MayorIgual(expr1, expr2);
            case MENOR:       return new Menor(expr1, expr2);
            case MENOR_IGUAL: return new MenorIgual(expr1, expr2);
            default: throw new IllegalStateException("Operador inesperado: " + op);
        }
    }

    /* Retorna el tipo de relación usado en el IR */
    public String getTipoCmp() {
        return getIzquierda().getTipo().equals(Tipo.FLOAT) ? "fcmp" : "icmp";
    }
}
