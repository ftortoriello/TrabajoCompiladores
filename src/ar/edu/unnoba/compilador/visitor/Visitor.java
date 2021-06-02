package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.*;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.*;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.*;
import ar.edu.unnoba.compilador.ast.sentencias.control.Control;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

public abstract class Visitor<T> {
    private int id = 0;

    protected int getID() {
        id += 1;
        return id;
    }


    // Visits base

    public T visit(Programa p) throws ExcepcionDeAlcance {
        T enc = p.getEncabezado().accept(this);
        T blq = p.getCuerpo().accept(this);
        return procesarPrograma(p, enc, blq);
    }

    public T visit(Encabezado e) throws ExcepcionDeAlcance {
        List<T> declaraciones = new ArrayList<>();
        for (Declaracion d : e.getDeclaraciones()) {
            declaraciones.add(d.accept(this));
        }
        return procesarEncabezado(e, declaraciones);
    }

    public T visit(Bloque b) throws ExcepcionDeAlcance {
        List<T> sentencias = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()) {
            sentencias.add(sentencia.accept(this));
        }
        return procesarBloque(b, sentencias);
    }

    public T visit(CasoCuando cc) throws ExcepcionDeAlcance {
        T expr = cc.getExpr().accept(this);
        T blq = cc.getBloque().accept(this);
        return procesarCasoCuando(cc, expr, blq);
    }

    public T visit(Literal c) {
        return procesarNodo(c);
    }

    public T visit(Identificador i) {
        return procesarNodo(i);
    }

    public T visit(InvocacionFuncion invo) {
        return procesarNodo(invo);
    }


    // Visits de operaciones

    public T visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        T ti = ob.getIzquierda().accept(this);
        T td = ob.getDerecha().accept(this);
        return this.procesarOperacionBinaria(ob, ti, td);
    }

    public T visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        return ou.getExpresion().accept(this);
    }


    // Visits de sentencias

    public T visit(Asignacion a) throws ExcepcionDeAlcance {
        T identificador = a.getIdent().accept(this);
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
        for (DecVar arg : df.getArgs()) {
            args.add(arg.accept(this));
        }
        T cuerpo = df.getBloque().accept(this);
        return procesarDecFuncion(args, cuerpo);
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

    public T visit(Cuando c) throws ExcepcionDeAlcance {
        T expr = c.getCondicion().accept(this);

        List<T> casosCuando = new ArrayList<>();
        for (CasoCuando caso : c.getCasos()) {
            casosCuando.add(caso.accept(this));
        }

        T blqElse = c.getBloqueElse().accept(this);

        return procesarCuando(c, expr, casosCuando, blqElse);
    }

    public T visit(Mientras m) throws ExcepcionDeAlcance {
        T expr = m.getCondicion().accept(this);
        T blq = m.getBloqueSentencias().accept(this);
        return procesarMientras(m, expr, blq);
    }

    public T visit(Para p) throws ExcepcionDeAlcance {
        return p.getBloqueSentencias().accept(this);
    }

    public T visit(Control c) {
        return procesarNodo(c);
    }

    public T visit(Retorno r) throws ExcepcionDeAlcance {
        return procesarRetorno(r, r.getExpr().accept(this));
    }


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

    protected abstract T procesarCuando(Cuando cc, T expr, List<T> casosCuando, T blqElse);

    protected abstract T procesarCasoCuando(CasoCuando cc, T expr, T blq);

    protected abstract T procesarMientras(Mientras m, T expr, T blq);

    protected abstract T procesarRetorno(Retorno r, T expr);
}
