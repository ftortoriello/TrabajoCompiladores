package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Visitor para verificar que las sentencias return, break y continue se encuentren en donde
 * corresponde.
 */
public class VisitorControl extends Visitor {

    /**
     * Flags para comprobar si el Visitor se encuentra en una función o estructura de control.
     * Para enBucle antes utilizábamos un booleano, pero necesitamos una pila porque si hay
     * estructuras de iteración anidadas al salir de la primera nos setea enBucle en falso, y
     * el resto de continue/break que haya los detecta como que no están en un bucle.
     */
    private boolean enFuncion;
    private Deque<Boolean> enBucle = new ArrayDeque<>();

    protected boolean isEnFuncion() {
        return enFuncion;
    }

    protected void setEnFuncion(boolean enFuncion) {
        this.enFuncion = enFuncion;
    }

    protected boolean enBucle() {
        return enBucle.peek();
    }

    public void visit(DecFun df) throws ExcepcionVisitor {
        setEnFuncion(true);
        super.visit(df);
        setEnFuncion(false);
    }

    public void visit(Mientras m) throws ExcepcionVisitor {
        enBucle.push(true);
        super.visit(m);
        enBucle.pop();
    }

    public void visit(Para p) throws ExcepcionVisitor {
        enBucle.push(true);
        super.visit(p);
        enBucle.pop();
    }

    @Override
    public void visit(Retorno r) throws ExcepcionVisitor {
        if (!isEnFuncion()) {
            throw new ExcepcionVisitor(r, "No se encuentra en una función");
        }
        super.visit(r);
    }

    // break
    @Override
    public void visit(Salir s) throws ExcepcionVisitor {
        if (!enBucle()) {
            throw new ExcepcionVisitor(s, "No se encuentra en un bucle for o while");
        }
        super.visit(s);
    }

    // continue
    @Override
    public void visit(Continuar c) throws ExcepcionVisitor {
        if (!enBucle()) {
            throw new ExcepcionVisitor(c, "No se encuentra en un bucle for o while");
        }
        super.visit(c);
    }
}
