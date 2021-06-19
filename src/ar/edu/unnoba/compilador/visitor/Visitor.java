package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.*;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

public abstract class Visitor {
    private int id = 0;

    /* Flags para comprobar si el Visitor se encuentra en una función o
     * estructura de control
     */
    private boolean enFuncion;
    private boolean enBucle;

    /* Funciones auxiliares */
    protected int getID() {
        id += 1;
        return id;
    }

    protected boolean isEnFuncion() {
        return enFuncion;
    }

    protected void setEnFuncion(boolean enFuncion) {
        this.enFuncion = enFuncion;
    }

    protected boolean isEnBucle() {
        return enBucle;
    }

    protected void setEnBucle(boolean enBucle) {
        this.enBucle = enBucle;
    }


    /*** Inicio del Visitor ***/
    public void procesar(Programa p) throws ExcepcionDeAlcance {
        p.accept(this);
    }


    /* Base */

    public void visit(Programa p) throws ExcepcionDeAlcance {
        p.getEncabezado().accept(this);
        p.getCuerpo().accept(this);
    }

    public void visit(Encabezado e) throws ExcepcionDeAlcance {
        for (Declaracion d : e.getDeclaraciones()) {
            d.accept(this);
        }
    }

    public void visit(Bloque b) throws ExcepcionDeAlcance {
        for (Nodo sentencia : b.getSentencias()) {
            sentencia.accept(this);
        }
    }


    /* Sentencia de asignación */

    public void visit(Asignacion a) throws ExcepcionDeAlcance {
        a.getIdent().accept(this);
        a.getExpresion().accept(this);
    }


    /* Sentencias de declaración */

    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        dv.getIdent().accept(this);
    }

    public void visit(DecVarIni dvi) throws ExcepcionDeAlcance {
        dvi.getIdent().accept(this);
        dvi.getExpresion().accept(this);
    }

    public void visit(DecFun df) throws ExcepcionDeAlcance {
        setEnFuncion(true);
        for (Param p : df.getParams()) {
            p.accept(this);
        }
        df.getBloque().accept(this);
        setEnFuncion(false);
    }

    public void visit(Param p) throws ExcepcionDeAlcance {
        p.getIdent().accept(this);
    }

    public void visit(ParamDef pd) throws ExcepcionDeAlcance {
        pd.getIdent().accept(this);
        pd.getExpresion().accept(this);
    }


    /* Sentencias de selección */

    public void visit(SiEntonces se) throws ExcepcionDeAlcance {
        se.getCondicion().accept(this);
        se.getBloqueSiEntonces().accept(this);
    }

    public void visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        ses.getCondicion().accept(this);
        ses.getBloqueSiEntonces().accept(this);
        ses.getBloqueSino().accept(this);
    }

    public void visit(Cuando c) throws ExcepcionDeAlcance {
        c.getCondicion().accept(this);
        for (CasoCuando caso : c.getCasos()) {
            caso.accept(this);
        }
        c.getBloqueElse().accept(this);
    }

    public void visit(CasoCuando cc) throws ExcepcionDeAlcance {
        cc.getExpresion().accept(this);
        cc.getBloque().accept(this);
    }


    /* Sentencias de iteración */

    public void visit(Mientras m) throws ExcepcionDeAlcance {
        setEnBucle(true);
        m.getCondicion().accept(this);
        m.getBloqueSentencias().accept(this);
        setEnBucle(false);
    }

    public void visit(Para p) throws ExcepcionDeAlcance {
        setEnBucle(true);
        p.getIdent().accept(this);
        p.getBloqueSentencias().accept(this);
        setEnBucle(false);
    }


    /* Sentencias de control */

    public void visit(Retorno r) throws ExcepcionDeAlcance {
        r.getExpresion().accept(this);
    }

    public void visit(Continuar c) throws ExcepcionDeAlcance {
    }

    public void visit(Salir s) throws ExcepcionDeAlcance {
    }


    /* Operaciones */

    public void visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        ob.getIzquierda().accept(this);
        ob.getDerecha().accept(this);
    }

    public void visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        ou.getExpresion().accept(this);
    }


    /* Valores */

    public void visit(Literal l) throws ExcepcionDeAlcance {
    }

    public void visit(Identificador i) throws ExcepcionDeAlcance {
    }

    public void visit(InvocacionFuncion i) throws ExcepcionDeAlcance {
        for (Expresion argumento : i.getArgs()) {
            argumento.accept(this);
        }
    }
}
