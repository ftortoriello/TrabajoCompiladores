package ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;

public abstract class OperacionConversion extends OperacionUnaria {
    public OperacionConversion(String nombre, Expresion expresion, Tipo tipo) {
        super(nombre, expresion, tipo);
    }

    public abstract String getInstruccionIR();

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }
}
