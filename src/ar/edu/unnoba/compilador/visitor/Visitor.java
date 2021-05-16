package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.instrucciones.*;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.*;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.*;

public abstract class Visitor<T> {

    private int iden = 0;

    protected int getID(){
        iden += 1;
        return iden;
    }

    // TODO: implementar la visita por cada tipo de nodo que la necesite

    // TODO: dejar la visita a Programa como estaba
    /*
    public T visit(Programa p) throws ExcepcionDeAlcance{
        // return p.getCuerpo();
        return p.getCuerpo().accept(this);
    }*/
    public String visit(Programa p) throws ExcepcionDeAlcance {
        return p.getCuerpo();
    }

    public T visit(Identificador i) {
        return procesarNodo(i);
    }

    public <T> T visit(Variable v) {
        throw new UnsupportedOperationException("Operación no soportada.");
    }

    public T visit(Constante c) {
        return procesarNodo(c);
    }

    protected abstract T procesarNodo(Nodo n);

    public T visit(OperacionBinaria ob) throws ExcepcionDeAlcance{
        T ti = ob.getIzquierda().accept(this);
        T td = ob.getDerecha().accept(this);
        return this.procesarOperacionBinaria(ob, ti, td);
    }

    public T visit(Asignacion a) throws ExcepcionDeAlcance{
        T identificador = a.getIdentificador().accept(this);
        T expresion = a.getExpresion().accept(this);
        return this.procesarAsignacion(a, identificador, expresion);
    }

    public T visit(DeclaracionVariable dv) throws ExcepcionDeAlcance{
        return dv.getId().accept(this);
    }

    public T visit(Bloque b) throws ExcepcionDeAlcance{
        List<T> result = new ArrayList<>();
        for (Sentencia sentencia : b.getSentencias()){
            result.add(sentencia.accept(this));
        }
        return procesarBloque(b, result);
    }

    public T visit(OperacionUnaria ou) throws ExcepcionDeAlcance{
        return ou.getExpresion().accept(this);
    }

    protected abstract T procesarBloque(Bloque bloque, List<T> sentencias);

    protected abstract T procesarOperacionBinaria(OperacionBinaria ob, T ei, T ed);

    protected abstract T procesarAsignacion(Asignacion a, T identificador, T expresion);
}