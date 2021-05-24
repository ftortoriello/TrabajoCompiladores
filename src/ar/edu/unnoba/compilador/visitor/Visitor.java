package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.*;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.*;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.*;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;

public abstract class Visitor<T> {

    private int iden = 0;

    protected int getID(){
        iden += 1;
        return iden;
    }

    // TODO: implementar la visita por cada tipo de nodo que la necesite

    // Visit Base
    public T visit(Programa p) throws ExcepcionDeAlcance {
        return p.getEncabezado().accept(this);

        // return p.getCuerpo().accept(this);
    }
    public T visit(Encabezado e) throws ExcepcionDeAlcance{
        List<T> result = new ArrayList<>();
        for (Sentencia s : e.getDeclaraciones()){
            result.add(s.accept(this));
        }
        return procesarEncabezado(e, result);
    }
    public T visit(Bloque b) throws ExcepcionDeAlcance{
        List<T> result = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()){
            result.add(sentencia.accept(this));
        }
        return procesarBloque(b, result);
    }
    public T visit(Constante c) {
        return procesarNodo(c);
    }
    public T visit(Identificador i) {
        return procesarNodo(i);
    }
    public T visit(InvocacionFuncion invo) {
        return procesarNodo(invo);
    }
    public <T> T visit(Variable v) {
        throw new UnsupportedOperationException("Operación no soportada.");
    }
    // ----------

    // Visit Operaciones
    public T visit(OperacionBinaria ob) throws ExcepcionDeAlcance{
        T ti = ob.getIzquierda().accept(this);
        T td = ob.getDerecha().accept(this);
        return this.procesarOperacionBinaria(ob, ti, td);
    }
    public T visit(OperacionUnaria ou) throws ExcepcionDeAlcance{
        return ou.getExpresion().accept(this);
    }
    // ----------

    // Visit Sentencias
    public T visit(Asignacion a) throws ExcepcionDeAlcance{
        T identificador = a.getIdentificador().accept(this);
        T expresion = a.getExpresion().accept(this);
        return this.procesarAsignacion(a, identificador, expresion);
    }
    public T visit(DecVar dv) throws ExcepcionDeAlcance{
        return dv.getIdent().accept(this);
    }
    public T visit(DecFuncion df) throws ExcepcionDeAlcance{
        return df.getIdent().accept(this);
    }
    // ----------

    // Procesos
    protected abstract T procesarNodo(Nodo n);
    protected abstract T procesarBloque(Bloque bloque, List<T> sentencias);
    protected abstract T procesarEncabezado(Encabezado encabezado, List<T> sentencias);
    protected abstract T procesarOperacionBinaria(OperacionBinaria ob, T ei, T ed);
    protected abstract T procesarAsignacion(Asignacion a, T identificador, T expresion);
    // ----------
}
