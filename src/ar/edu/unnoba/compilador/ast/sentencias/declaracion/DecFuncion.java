package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecFuncion extends Declaracion {
    private ArrayList<DecVar> args;
    private Bloque bloque;
    private Alcance alcance;

    public DecFuncion(Identificador ident, ArrayList<DecVar> args, Bloque bloque) {
        super(ident.getNombre(), ident);
        // Invierto el orden de los argumentos porque debido a la forma de las reglas los lee al revés
        Collections.reverse(args);
        bloque.setNombre("Cuerpo\nFUNCIÓN");
        this.args = args;
        this.bloque = bloque;
    }

    public DecFuncion(Identificador ident, Bloque bloque) {
        super(ident.getNombre(), ident);
        bloque.setNombre("Cuerpo\nFUNCIÓN");
        this.args = new ArrayList<>();
        this.bloque = bloque;
    }

    public ArrayList<DecVar> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<DecVar> args) {
        this.args = args;
    }

    public Bloque getBloque() {
        return bloque;
    }

    public void setBloque(Bloque bloque) {
        this.bloque = bloque;
    }

    public Alcance getAlcance() {
        return alcance;
    }

    public void setAlcance(Alcance alcance) {
        this.alcance = alcance;
    }

    /* Retorna la cantidad de argumentos no opcionales de la declaración.
     * Usado para validar el pasaje de parámetros.
     */
    public int getCantArgsObligatorios() {
        int cant = 0;
        for (DecVar dv : args) {
            if (dv instanceof DecVarInicializada) {
                // Se encontró una variable con parámetro opcional. A partir de acá son todos opcionales.
                break;
            }
            cant++;
        }
        return cant;
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public DecFuncion accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s %s()\n<%s>", getClass().getSimpleName(),
                getIdent().getNombre(), getTipo());
    }
}
