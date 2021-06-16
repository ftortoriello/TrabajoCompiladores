package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
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

public abstract class Transformer {
    // Guardamos la última función visitada para validar el tipo de retorno,
    // y convertir el tipo de la expresión return en caso de ser necesario
    private DecFuncion ultFunVisitada;

    protected DecFuncion getUltFunVisitada() {
        return ultFunVisitada;
    }

    protected void setUltFunVisitada(DecFuncion ultFunVisitada) {
        this.ultFunVisitada = ultFunVisitada;
    }


    public Programa procesar(Programa p) throws ExcepcionDeTipos {
        return p.accept(this);
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

    public Sentencia transform(CasoCuando cc) throws ExcepcionDeTipos {
        cc.setExpr(cc.getExpr().accept(this));
        cc.setBloque(cc.getBloque().accept(this));
        return cc;
    }

    public Literal transform(Literal c) {
        return c;
    }

    public Identificador transform(Identificador i) throws ExcepcionDeTipos {
        return i;
    }

    public InvocacionFuncion transform(InvocacionFuncion invo) throws ExcepcionDeTipos {
        List<Expresion> argumentos = new ArrayList<>();
        for (Expresion argumento : invo.getArgs()) {
            argumentos.add(argumento.accept(this));
        }
        invo.setArgumentos(argumentos);
        return invo;
    }


    // Transforms de operaciones

    public FlotanteAEntero transform(FlotanteAEntero fae) throws ExcepcionDeTipos {
        fae.setExpresion(fae.getExpresion().accept(this));
        return fae;
    }

    public EnteroAFlotante transform(EnteroAFlotante eaf) throws ExcepcionDeTipos {
        eaf.setExpresion(eaf.getExpresion().accept(this));
        return eaf;
    }

    public Expresion transform(OperacionBinaria ob) throws ExcepcionDeTipos {
        ob.setIzquierda(ob.getIzquierda().accept(this));
        ob.setDerecha(ob.getDerecha().accept(this));
        return ob;
    }

    public Expresion transform(Relacion r) throws ExcepcionDeTipos {
        r.setIzquierda(r.getIzquierda().accept(this));
        r.setDerecha(r.getDerecha().accept(this));
        return r;
    }

    public Expresion transform(OperacionUnaria ou) throws ExcepcionDeTipos {
        ou.setExpresion(ou.getExpresion().accept(this));
        return ou;
    }

    // Transforms de sentencias

    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos {
        a.setIdent(a.getIdent().accept(this));
        a.setExpresion(a.getExpresion().accept(this));
        return a;
    }

    public DecVar transform(DecVar dv) throws ExcepcionDeTipos {
        dv.setIdent(dv.getIdent().accept(this));
        return dv;
    }

    public DecVarInicializada transform(DecVarInicializada dvi) throws ExcepcionDeTipos {
        dvi.setIdent(dvi.getIdent().accept(this));
        dvi.setExpresion(dvi.getExpresion().accept(this));
        return dvi;
    }

    public DecFuncion transform(DecFuncion df) throws ExcepcionDeTipos {
        setUltFunVisitada(df);

        ArrayList<DecVar> args = new ArrayList<>();
        for (DecVar arg : df.getArgs()) {
            args.add(arg.accept(this));
        }
        df.setArgs(args);
        df.setBloque(df.getBloque().accept(this));
        setUltFunVisitada(null);
        return df;
    }

    public SiEntonces transform(SiEntonces se) throws ExcepcionDeTipos {
        se.setCondicion(se.getCondicion().accept(this));
        se.setBloqueSiEntonces(se.getBloqueSiEntonces().accept(this));
        return se;
    }

    public SiEntoncesSino transform(SiEntoncesSino ses) throws ExcepcionDeTipos {
        ses.setCondicion(ses.getCondicion().accept(this));
        ses.setBloqueSiEntonces(ses.getBloqueSiEntonces().accept(this));
        ses.setBloqueSino(ses.getBloqueSino().accept(this));
        return ses;
    }

    public Nodo transform(Cuando c) throws ExcepcionDeTipos {
        c.setCondicion(c.getCondicion().accept(this));

        List<CasoCuando> casosCuando = new ArrayList<>();
        for (CasoCuando caso : c.getCasos()) {
            casosCuando.add((CasoCuando) caso.accept(this));
        }
        c.setCasos(casosCuando);

        c.setBloqueElse(c.getBloqueElse().accept(this));
        return c;
    }

    public Mientras transform(Mientras m) throws ExcepcionDeTipos {
        m.setCondicion(m.getCondicion().accept(this));
        m.setBloqueSentencias(m.getBloqueSentencias().accept(this));
        return m;
    }

    public Nodo transform(Para p) throws ExcepcionDeTipos {
        p.setIdent(p.getIdent().accept(this));
        p.setBloqueSentencias(p.getBloqueSentencias().accept(this));
        return p;
    }

    public Retorno transform(Retorno r) throws ExcepcionDeTipos {
        r.setExpr(r.getExpr().accept(this));
        return r;
    }

    public Salir transform(Salir s) throws ExcepcionDeTipos {
        return s;
    }

    public Continuar transform(Continuar c) throws ExcepcionDeTipos {
        return c;
    }
}
