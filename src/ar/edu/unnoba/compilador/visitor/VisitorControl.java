package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;

/**
 * Visitor para verificar que las sentencias return, break y continue se encuentren en donde
 * corresponde.
 */
public class VisitorControl extends Visitor {
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
        if (!isEnBucle()) {
            throw new ExcepcionVisitor(s, "No se encuentra en un bucle for o while");
        }
        super.visit(s);
    }

    // continue
    @Override
    public void visit(Continuar c) throws ExcepcionVisitor {
        if (!isEnBucle()) {
            throw new ExcepcionVisitor(c, "No se encuentra en un bucle for o while");
        }
        super.visit(c);
    }
}
