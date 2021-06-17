package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;

public class MenorIgual extends Relacion {
    public MenorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha, "<=");
    }

    @Override
    public String getInstruccionIR() {
        return "sle";
    }
}
