package ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Booleano;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class NegacionLogica extends OperacionUnaria {
    public NegacionLogica(Expresion expresion) {
        super("NOT", expresion, Tipo.BOOLEAN);
    }

    /**
     * Propiedad para detectar si se debe generar código IR para los NOT.
     * No hay que hacerlo si se invierten las etiquetas de una condición.
     */
    private boolean enCortocircuito = false;

    @Override
    public String getRefIR() {
        // Como implementamos cortocircuito booleano, no le generamos una referencia a los NOT,
        // invertimos las etiquetas de salto.
        // Retornar la referencia de la expresión.
        return getExpresion().getRefIR();
    }

    public Expresion evaluar() {
        Expresion expr = getExpresion();
        if (expr instanceof NegacionLogica) {
            // cancelar "not not"
            return ((NegacionLogica) expr).getExpresion();
        }

        if (!(expr instanceof Booleano)) {
            return this;
        }

        boolean valorNegado = !((Booleano) getExpresion()).getValor();
        return new Booleano(valorNegado);
    }

    public boolean isEnCortocircuito() {
        return enCortocircuito;
    }

    public void setEnCortocircuito(boolean enCortocircuito) {
        this.enCortocircuito = enCortocircuito;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }
}
