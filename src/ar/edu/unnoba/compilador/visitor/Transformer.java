package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Encabezado;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Simbolo;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Control;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

public abstract class Transformer {

    // Guardamos la última función visitada para validar el tipo de retorno,
    // y convertir el tipo de la expresión return en caso de ser necesario
    private DecFuncion ultFunVisitada;

    public DecFuncion getUltFunVisitada() {
        return ultFunVisitada;
    }

    public void setUltFunVisitada(DecFuncion ultFunVisitada) {
        this.ultFunVisitada = ultFunVisitada;
    }

    // Transforms base

    public Programa transform(Programa p) throws ExcepcionDeTipos {
        p.setEncabezado(p.getEncabezado().accept(this));
        p.setCuerpo(p.getCuerpo().accept(this));
        return p;
    }

    public Encabezado transform(Encabezado e) throws ExcepcionDeTipos {
        List<Declaracion> declaraciones = new ArrayList<>();
        for (Declaracion d : e.getDeclaraciones()) {
            declaraciones.add((Declaracion) d.accept(this));
        }
        e.setDeclaraciones(declaraciones);
        return e;
    }

    public Bloque transform(Bloque b) throws ExcepcionDeTipos {
        List<Nodo> sentencias = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()) {
            sentencias.add(sentencia.accept(this));
        }
        b.setSentencias(sentencias);
        return b;
    }

    public CasoCuando transform(CasoCuando cc) throws ExcepcionDeTipos {
        cc.setExpr((Expresion) cc.getExpr().accept(this));
        cc.setBloque(cc.getBloque().accept(this));
        return cc;
    }

    public Literal transform(Literal c) throws ExcepcionDeTipos {
        return c;
    }

    public Identificador transform(Identificador i) throws ExcepcionDeTipos {
        return i;
    }

    public Simbolo transform(Simbolo s) throws ExcepcionDeTipos {
        return s;
    }

    public InvocacionFuncion transform(InvocacionFuncion invo) throws ExcepcionDeTipos {
        return invo;
    }


    // Transforms de operaciones

    public FlotanteAEntero transform(FlotanteAEntero fae) throws ExcepcionDeTipos {
        fae.setExpresion((Expresion) fae.getExpresion().accept(this));
        return fae;
    }

    public EnteroAFlotante transform(EnteroAFlotante eaf) throws ExcepcionDeTipos {
        eaf.setExpresion((Expresion) eaf.getExpresion().accept(this));
        return eaf;
    }

    public OperacionBinaria transform(OperacionBinaria ob) throws ExcepcionDeTipos {
        ob.setIzquierda((Expresion) ob.getIzquierda().accept(this));
        ob.setDerecha((Expresion) ob.getDerecha().accept(this));
        return ob;
    }

    public Relacion transform(Relacion r) throws ExcepcionDeTipos {
        r.setIzquierda((Expresion) r.getIzquierda().accept(this));
        r.setDerecha((Expresion) r.getDerecha().accept(this));
        return r;
    }

    public OperacionUnaria transform(OperacionUnaria ou) throws ExcepcionDeTipos {
        ou.setExpresion((Expresion) ou.getExpresion().accept(this));
        return ou;
    }

    // Transforms de sentencias

    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos {
        a.setIdent(a.getIdent().accept(this));
        a.setExpresion((Expresion) a.getExpresion().accept(this));
        return a;
    }

    public DecVar transform(DecVar dv) throws ExcepcionDeTipos {
        dv.setIdent(dv.getIdent().accept(this));
        return dv;
    }

    public DecVarInicializada transform(DecVarInicializada dvi) throws ExcepcionDeTipos {
        dvi.setIdent(dvi.getIdent().accept(this));
        dvi.setExpresion((Expresion) dvi.getExpresion().accept(this));
        return dvi;
    }

    public DecFuncion transform(DecFuncion df) throws ExcepcionDeTipos {
        setUltFunVisitada(df);

        List<DecVar> args = new ArrayList<>();
        for (DecVar arg : df.getArgs()) {
            args.add(arg.accept(this));
        }
        df.setArgs(args);
        df.setBloque(df.getBloque().accept(this));
        setUltFunVisitada(null);
        return df;
    }

    public SiEntonces transform(SiEntonces se) throws ExcepcionDeTipos {
        se.setCondicion((Expresion) se.getCondicion().accept(this));
        se.setBloqueSiEntonces(se.getBloqueSiEntonces().accept(this));
        return se;
    }

    public SiEntoncesSino transform(SiEntoncesSino ses) throws ExcepcionDeTipos {
        ses.setCondicion((Expresion) ses.getCondicion().accept(this));
        ses.setBloqueSiEntonces(ses.getBloqueSiEntonces().accept(this));
        ses.setBloqueSino(ses.getBloqueSino().accept(this));
        return ses;
    }

    public Cuando transform(Cuando c) throws ExcepcionDeTipos {
        c.setCondicion((Expresion) c.getCondicion().accept(this));

        List<CasoCuando> casosCuando = new ArrayList<>();
        for (CasoCuando caso : c.getCasos()) {
            casosCuando.add(caso.accept(this));
        }
        c.setCasos(casosCuando);

        c.setBloqueElse(c.getBloqueElse().accept(this));
        return c;
    }

    public Mientras transform(Mientras m) throws ExcepcionDeTipos {
        m.setCondicion((Expresion) m.getCondicion().accept(this));
        m.setBloqueSentencias(m.getBloqueSentencias().accept(this));
        return m;
    }

    public Para transform(Para p) throws ExcepcionDeTipos {
        p.setBloqueSentencias(p.getBloqueSentencias().accept(this));
        return p;
    }

    public Control transform(Control c) throws ExcepcionDeTipos {
        return c;
    }

    public Retorno transform(Retorno r) throws ExcepcionDeTipos {
        r.setExpr((Expresion) r.getExpr().accept(this));
        return r;
    }
}
