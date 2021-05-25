package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.logicas.Disyuncion;
import ar.edu.unnoba.compilador.ast.sentencias.*;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.*;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.*;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

public abstract class Visitor<T> {

    private int iden = 0;

    protected int getID(){
        iden += 1;
        return iden;
    }

    // TODO: implementar la visita por cada tipo de nodo que la necesite

    // Visit Base
    public T visit(Programa p) throws ExcepcionDeAlcance {
        T enc = p.getEncabezado().accept(this);
        T blq = p.getCuerpo().accept(this);
        return procesarPrograma(p, enc, blq);
    }

    public T visit(Encabezado e) throws ExcepcionDeAlcance {
        List<T> result = new ArrayList<>();
        for (Sentencia s : e.getDeclaraciones()){
            result.add(s.accept(this));
        }
        return procesarEncabezado(e, result);
    }
    public T visit(Bloque b) throws ExcepcionDeAlcance {
        List<T> result = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()){
            result.add(sentencia.accept(this));
        }
        return procesarBloque(b, result);
    }

    public T visit(Constante c) throws ExcepcionDeAlcance {
        return procesarNodo(c);
    }
    public T visit(Identificador i) throws ExcepcionDeAlcance {
        return procesarNodo(i);
    }
    public T visit(InvocacionFuncion invo) throws ExcepcionDeAlcance {
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
    public T visit(Asignacion a) throws ExcepcionDeAlcance {
        T identificador = a.getIdentificador().accept(this);
        T expresion = a.getExpresion().accept(this);
        return this.procesarAsignacion(a, identificador, expresion);
    }
    public T visit(DecVar dv) throws ExcepcionDeAlcance {
        return dv.getIdent().accept(this);
    }
    public T visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        T ident = dvi.getIdent().accept(this);
        T expr = dvi.getExpresion().accept(this);
        return this.procesarVarInicializada(ident, expr);
    }
    public T visit(DecFuncion df) throws ExcepcionDeAlcance {
        List<T> args = new ArrayList<>();
        for (DecVar arg : df.getArgs()){
            args.add(arg.accept(this));
        }

        T cuerpo = df.getBloque().accept(this);

        return procesarDecFuncion(args, cuerpo);
    }

    public T visit(Mientras m) throws ExcepcionDeAlcance {
        return null;
    }
    public T visit(Para p) throws ExcepcionDeAlcance {
        return null;
    }

    public T visit(Cuando c) throws ExcepcionDeAlcance {
        return null;
    }
    public T visit(SiEntonces se) throws ExcepcionDeAlcance {
        T cond = se.getCondicion().accept(this);
        T blqSi = se.getBloqueSiEntonces().accept(this);
        return procesarSiEntonces(cond, blqSi);
    }
    public T visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        T cond = ses.getCondicion().accept(this);
        T blqSi = ses.getBloqueSiEntonces().accept(this);
        T blqSino = ses.getBloqueSino().accept(this);
        return procesarSiEntoncesSino(cond, blqSi, blqSino);
    }

    public T visit(Continuar c) throws ExcepcionDeAlcance {
        return null;
    }
    public T visit(Retorno r) throws ExcepcionDeAlcance {
        return null;
    }
    public T visit(Salir s) throws ExcepcionDeAlcance {
        return null;
    }
    // ----------

    // Procesos implementados en los visitors específicos
    protected abstract T procesarNodo(Nodo n);
    protected abstract T procesarPrograma(Programa p, T enc, T blq);
    protected abstract T procesarBloque(Bloque bloque, List<T> sentencias);
    protected abstract T procesarEncabezado(Encabezado encabezado, List<T> sentencias);
    protected abstract T procesarOperacionBinaria(OperacionBinaria ob, T ei, T ed);
    protected abstract T procesarVarInicializada(T ident, T expr);
    protected abstract T procesarAsignacion(Asignacion a, T identificador, T expresion);
    protected abstract T procesarDecFuncion(List<T> args, T cuerpo);
    protected abstract T procesarSiEntonces(T cond, T blq);
    protected abstract T procesarSiEntoncesSino(T cond, T blqSi, T blqSino);
    // ----------
}
