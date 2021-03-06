package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Encabezado;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas.NegacionAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.OperacionConversion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.Cadena;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;

public abstract class Visitor {
    private int id = 0;

    /* Funciones auxiliares */
    protected int getID() {
        id += 1;
        return id;
    }

    /**
     * Inicio del Visitor
     */
    public void procesar(Programa p) throws ExcepcionVisitor {
        p.accept(this);
    }


    // *** Base ***

    public void visit(Programa p) throws ExcepcionVisitor {
        p.getEncabezado().accept(this);
        p.getCuerpo().accept(this);
    }

    public void visit(Encabezado e) throws ExcepcionVisitor {
        for (Declaracion d : e.getDeclaraciones()) {
            d.accept(this);
        }
    }

    public void visit(Bloque b) throws ExcepcionVisitor {
        for (Nodo sentencia : b.getSentencias()) {
            sentencia.accept(this);
        }
    }


    // *** Sentencia de asignaci??n ***

    public void visit(Asignacion a) throws ExcepcionVisitor {
        a.getIdent().accept(this);
        a.getExpresion().accept(this);
    }


    // *** Sentencias de declaraci??n ***

    public void visit(DecVar dv) throws ExcepcionVisitor {
        dv.getIdent().accept(this);
    }

    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        dvi.getIdent().accept(this);
        dvi.getExpresion().accept(this);
    }

    public void visit(DecFun df) throws ExcepcionVisitor {
        for (Param p : df.getParams()) {
            p.accept(this);
        }
        df.getBloque().accept(this);
    }

    public void visit(Param p) throws ExcepcionVisitor {
        p.getIdent().accept(this);
    }

    public void visit(ParamDef pd) throws ExcepcionVisitor {
        pd.getIdent().accept(this);
        pd.getExpresion().accept(this);
    }


    // *** Sentencias de selecci??n ***

    public void visit(SiEntonces se) throws ExcepcionVisitor {
        se.getCondicion().accept(this);
        se.getBloqueSiEntonces().accept(this);
    }

    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        ses.getCondicion().accept(this);
        ses.getBloqueSiEntonces().accept(this);
        ses.getBloqueSino().accept(this);
    }

    public void visit(Cuando c) throws ExcepcionVisitor {
        c.getCondicion().accept(this);
        for (CasoCuando caso : c.getCasos()) {
            caso.accept(this);
        }
        c.getBloqueElse().accept(this);
    }

    public void visit(CasoCuando cc) throws ExcepcionVisitor {
        cc.getExpresion().accept(this);
        cc.getBloque().accept(this);
    }


    // *** Sentencias de iteraci??n ***

    public void visit(Mientras m) throws ExcepcionVisitor {
        m.getCondicion().accept(this);
        m.getBloqueSentencias().accept(this);
    }


    // *** Sentencias de control ***

    public void visit(Retorno r) throws ExcepcionVisitor {
        r.getExpresion().accept(this);
    }

    public void visit(Continuar c) throws ExcepcionVisitor {
    }

    public void visit(Salir s) throws ExcepcionVisitor {
    }


    // *** Operaciones ***

    public void visit(OperacionBinaria ob) throws ExcepcionVisitor {
        ob.getIzquierda().accept(this);
        ob.getDerecha().accept(this);
    }

    public void visit(NegacionAritmetica neg) throws ExcepcionVisitor {
        neg.getExpresion().accept(this);
    }

    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        neg.getExpresion().accept(this);
    }

    public void visit(OperacionConversion conv) throws ExcepcionVisitor {
        conv.getExpresion().accept(this);
    }


    // *** Valores ***

    public void visit(Literal l) {
    }

    public void visit(Identificador i) throws ExcepcionVisitor {
    }

    public void visit(InvocacionFuncion i) throws ExcepcionVisitor {
        for (Expresion argumento : i.getArgs()) {
            argumento.accept(this);
        }
    }

    public void visit(Cadena c) {
    }
}
