package ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

public abstract class OperacionBinariaAritmetica extends OperacionBinaria {

    public OperacionBinariaAritmetica(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }

    @Override
    public Expresion evaluar() throws ExcepcionTransformer {
        Tipo tipo = getIzquierda().getTipo();
        // Obtener expresiones evaluadas
        Expresion izquierda = getIzquierda().evaluar();
        Expresion derecha = getDerecha().evaluar();

        // Obtener valores numéricos, y si es posible eliminar elementos neutros (ej: x + 0),
        // o retonar un literal "0" si hay un elemento nulo (ej: x * 0).
        Number valorIzq = 0;
        if (izquierda instanceof Literal) {
            valorIzq = ((Literal) izquierda).getValorNumerico();
            if (esElementoNeutroIzq(valorIzq)) return derecha;
            if (esElementoNuloIzq(valorIzq)) return new Literal(0, tipo);
        }

        Number valorDer;
        if (derecha instanceof Literal) {
            valorDer = ((Literal) derecha).getValorNumerico();
            if (esElementoNeutroDer(valorDer)) return izquierda;
            if (esElementoNuloDer(valorDer)) return new Literal(0, tipo);
        } else {
            // El operando derecho no es constante; no se puede seguir evaluando
            return this;
        }

        if (izquierda instanceof Literal) {
            // Los dos operandos son literales (ej: 1 + 2)
            return new Literal(calcularResultado(valorIzq, valorDer), tipo);
        }

        // Esto es para soportar casos donde un hijo de la subexpresión izquierda es literal,
        // por ejemplo x + 1 + 2.
        if (!((izquierda instanceof Suma || izquierda instanceof Resta) &&
                (this instanceof Suma || this instanceof Resta))) {
            return this;
        }

        // Son operadores compatibles.
        // Fijarse si hay literales en los hijos de la rama izquierda.
        OperacionBinariaAritmetica opIzquierda = (OperacionBinariaAritmetica) izquierda;
        Expresion subexprIzq = opIzquierda.getIzquierda();
        Expresion subexprDer = opIzquierda.getDerecha();

        if (subexprIzq instanceof Literal) {
            valorIzq = ((Literal) subexprIzq).getValorNumerico();
            // Reemplazar el nodo izquierdo por el derecho del subárbol (el que NO tiene el literal)
            setIzquierda(subexprDer);
            // Y hacer constant folding del lado derecho con el literal encontrado
            setDerecha(new Literal(calcularResultado(valorIzq, valorDer), tipo));
            return this;
        }

        // Hacer lo mismo con el hijo derecho
        if (subexprDer instanceof Literal) {
            valorIzq = ((Literal) subexprDer).getValorNumerico();
            if (izquierda instanceof Resta) valorIzq = -valorIzq.doubleValue();
            setIzquierda(subexprIzq);
            setDerecha(new Literal(calcularResultado(valorIzq, valorDer), tipo));
            return this;
        }

        return this;
    }

    protected abstract Number calcularResultado(Number izq, Number der) throws ExcepcionTransformer;

    protected abstract boolean esElementoNeutroIzq(Number numero);
    protected abstract boolean esElementoNeutroDer(Number numero);

    protected abstract boolean esElementoNuloIzq(Number numero);
    protected abstract boolean esElementoNuloDer(Number numero);
}
