package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.Expresion;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class DecVarInicializada extends DecVar {

    private Expresion expresion;

    public DecVarInicializada(String nombre, Identificador id, Expresion expr) {
        super(nombre, id);
        this.expresion = expr;
        // TODO: ¿Y si el tipo del Identificador es distinto al de la Expresión?
        }

    public Expresion getExpresion() {
        return expresion;
    }

    public void setExpresion(Expresion expresion) {
        this.expresion = expresion;
    }

}
