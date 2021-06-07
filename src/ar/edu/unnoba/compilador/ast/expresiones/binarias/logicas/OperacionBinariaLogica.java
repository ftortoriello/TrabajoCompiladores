package ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class OperacionBinariaLogica extends OperacionBinaria {

    public OperacionBinariaLogica(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }


    @Override
    public Expresion evaluar() {
        if (!(getIzquierda().evaluar() instanceof Literal) || !(getDerecha().evaluar() instanceof Literal)) {
            return this;
        }

        boolean litIzq = Boolean.parseBoolean(((Literal) getIzquierda().evaluar()).getValor());
        boolean litDer = Boolean.parseBoolean(((Literal) getDerecha().evaluar()).getValor());
        boolean resultado = calcularResultado(litIzq, litDer);

        return new Literal(String.valueOf(resultado), Tipo.BOOLEAN, "Literal BOOLEAN");
    }

    private Boolean calcularResultado(Boolean litIzq, Boolean litDer) {
        switch (getNombre()) {
            case "AND": return litIzq && litDer;
            case "OR":  return litIzq || litDer;
            default:
                throw new IllegalStateException("Operador inesperado al aplicar Constant Folding: " + getNombre());
        }
    }
}
