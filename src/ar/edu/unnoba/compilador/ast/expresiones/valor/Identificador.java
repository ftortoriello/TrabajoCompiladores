package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.Transformer;

/* Clase para catalogar en la primera pasada cosas que no sabemos si son
 * variables, funciones...
 * Después se reemplazan por Simbolo. */
public class Identificador extends Valor {

    private String nombreIR = null;

    public Identificador(String nombre) {
        super(Tipo.UNKNOWN, nombre);
    }

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    public Identificador(String nombre, Tipo tipo) {
        super(tipo, nombre);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s\n<%s>", getNombre(), getTipo());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Identificador accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    public Expresion evaluar() {
        return this;
    }
}
