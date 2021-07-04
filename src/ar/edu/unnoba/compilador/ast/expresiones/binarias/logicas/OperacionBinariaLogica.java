package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Booleano;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

public abstract class OperacionBinariaLogica extends OperacionBinaria {
    public OperacionBinariaLogica(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Expresion izquierda = getIzquierda().evaluar();
        Expresion derecha = getDerecha().evaluar();

        if (izquierda instanceof Booleano) {
            // Si el valor determina a la expresión (ej: true and...), retornar un nuevo literal.
            // Si no, es neutro (ej: or and...). En ese caso, retornar el otro operando.
            return transformarResultado(((Booleano) izquierda).getValor(), derecha);
        }

        if (derecha instanceof Literal) {
            return transformarResultado(((Booleano) derecha).getValor(), izquierda);
        }

        // Ningún operando es literal
        return this;
    }

    protected abstract Expresion transformarResultado(boolean valor, Expresion otroOperando);
}
