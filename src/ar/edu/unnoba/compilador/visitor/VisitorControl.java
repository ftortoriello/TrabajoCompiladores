package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;

/* Visitor para verificar que las sentencias return, break y continue se
 * encuentren en donde corresponde.
 */
public class VisitorControl extends Visitor {
    @Override
    public void visit(Retorno r) throws ExcepcionDeAlcance {
        if (!isEnFuncion()) {
            throw new ExcepcionDeAlcance("Se encontró «return» fuera de una función");
        }
        super.visit(r);
    }

    // break
    @Override
    public void visit(Salir s) throws ExcepcionDeAlcance {
        if (!isEnBucle()) {
            throw new ExcepcionDeAlcance("Se encontró «break» fuera de un bucle for o while");
        }
        super.visit(s);
    }

    // continue
    @Override
    public void visit(Continuar r) throws ExcepcionDeAlcance {
        if (!isEnBucle()) {
            throw new ExcepcionDeAlcance("Se encontró «continue» fuera de un bucle for o while");
        }
        super.visit(r);
    }
}
