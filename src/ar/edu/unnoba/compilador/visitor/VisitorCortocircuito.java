package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;

/**
 * Visitor auxiliar para implementar el cortocircuito booleano.
 * Recorre el AST, y si una operación lógica es la condición de una estructura,
 * pone en verdadero el atributo enCortocircuito, que será utilizado a la hora
 * de generar el código.
 */
public class VisitorCortocircuito extends Visitor {
    private boolean enEstructura;

    public boolean enEstructura() {
        return enEstructura;
    }

    public void setEnEstructura(boolean enEstructura) {
        this.enEstructura = enEstructura;
    }

    public void procesar(Programa p) throws ExcepcionVisitor {
        p.accept(this);
    }

    @Override
    public void visit(SiEntonces se) throws ExcepcionVisitor {
        if (enEstructura) {
            // Si ya está en una estructura no toco el valor, solamente se setea
            // la primera vez que entra y cuando sale completamente de la estructura.
            super.visit(se);
        } else {
            setEnEstructura(true);
            super.visit(se);
            setEnEstructura(false);
        }
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        if (enEstructura) {
            super.visit(ses);
        } else {
            setEnEstructura(true);
            super.visit(ses);
            setEnEstructura(false);
        }
    }


    @Override
    public void visit(Mientras m) throws ExcepcionVisitor {
        if (enEstructura) {
            super.visit(m);
        } else {
            setEnEstructura(true);
            super.visit(m);
            setEnEstructura(false);
        }
    }

    @Override
    public void visit(OperacionBinaria op) throws ExcepcionVisitor {
        if (enEstructura() && op instanceof OperacionBinariaLogica) {
            op.setEnCortocircuito(true);
        }
        super.visit(op);
    }

    @Override
    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        if (enEstructura()) {
            neg.setEnCortocircuito(true);
        }
        super.visit(neg);
    }


}
