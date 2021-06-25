package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Encabezado;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
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

import java.util.ArrayList;
import java.util.List;

public abstract class Transformer {
    // Guardamos la última función visitada para validar el tipo de retorno,
    // y convertir el tipo de la expresión return en caso de ser necesario
    private DecFun ultFunVisitada;

    protected DecFun getUltFunVisitada() {
        return ultFunVisitada;
    }

    protected void setUltFunVisitada(DecFun ultFunVisitada) {
        this.ultFunVisitada = ultFunVisitada;
    }


    public Programa procesar(Programa p) throws ExcepcionTransformer {
        return p.accept(this);
    }

    // Transforms base

    public Programa transform(Programa p) throws ExcepcionTransformer {
        p.setEncabezado(p.getEncabezado().accept(this));
        p.setCuerpo(p.getCuerpo().accept(this));
        return p;
    }

    public Encabezado transform(Encabezado e) throws ExcepcionTransformer {
        List<Declaracion> declaraciones = new ArrayList<>();
        for (Declaracion d : e.getDeclaraciones()) {
            declaraciones.add((Declaracion) d.accept(this));
        }
        e.setDeclaraciones(declaraciones);
        return e;
    }

    public Bloque transform(Bloque b) throws ExcepcionTransformer {
        List<Nodo> sentencias = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()) {
            sentencias.add(sentencia.accept(this));
        }
        b.setSentencias(sentencias);
        return b;
    }

    public Sentencia transform(CasoCuando cc) throws ExcepcionTransformer {
        cc.setExpresion(cc.getExpresion().accept(this));
        cc.setBloque(cc.getBloque().accept(this));
        return cc;
    }

    public Literal transform(Literal c) {
        return c;
    }

    public Identificador transform(Identificador i) throws ExcepcionTransformer {
        return i;
    }

    public InvocacionFuncion transform(InvocacionFuncion invo) throws ExcepcionTransformer {
        List<Expresion> argumentos = new ArrayList<>();
        for (Expresion argumento : invo.getArgs()) {
            argumentos.add(argumento.accept(this));
        }
        invo.setArgumentos(argumentos);
        return invo;
    }


    // Transforms de operaciones

    public Expresion transform(OperacionBinaria ob) throws ExcepcionTransformer {
        ob.setIzquierda(ob.getIzquierda().accept(this));
        ob.setDerecha(ob.getDerecha().accept(this));
        return ob;
    }

    public Expresion transform(Relacion r) throws ExcepcionTransformer {
        r.setIzquierda(r.getIzquierda().accept(this));
        r.setDerecha(r.getDerecha().accept(this));
        return r;
    }

    public Expresion transform(OperacionUnaria ou) throws ExcepcionTransformer {
        ou.setExpresion(ou.getExpresion().accept(this));
        return ou;
    }

    // Transforms de sentencias

    public Asignacion transform(Asignacion a) throws ExcepcionTransformer {
        a.setIdent(a.getIdent().accept(this));
        a.setExpresion(a.getExpresion().accept(this));
        return a;
    }

    public DecVar transform(DecVar dv) throws ExcepcionTransformer {
        dv.setIdent(dv.getIdent().accept(this));
        return dv;
    }

    public DecVarIni transform(DecVarIni dvi) throws ExcepcionTransformer {
        dvi.setIdent(dvi.getIdent().accept(this));
        dvi.setExpresion(dvi.getExpresion().accept(this));
        return dvi;
    }

    public Param transform(Param p) throws ExcepcionTransformer {
        p.setIdent(p.getIdent().accept(this));
        return p;
    }

    public ParamDef transform(ParamDef pi) throws ExcepcionTransformer {
        pi.setIdent(pi.getIdent().accept(this));
        pi.setExpresion(pi.getExpresion().accept(this));
        return pi;
    }

    public DecFun transform(DecFun df) throws ExcepcionTransformer {
        setUltFunVisitada(df);

        ArrayList<Param> params = new ArrayList<>();
        for (Param p : df.getParams()) {
            params.add(p.accept(this));
        }
        df.setParams(params);
        df.setBloque(df.getBloque().accept(this));
        setUltFunVisitada(null);
        return df;
    }

    public SiEntonces transform(SiEntonces se) throws ExcepcionTransformer {
        se.setCondicion(se.getCondicion().accept(this));
        se.setBloqueSiEntonces(se.getBloqueSiEntonces().accept(this));
        return se;
    }

    public SiEntoncesSino transform(SiEntoncesSino ses) throws ExcepcionTransformer {
        ses.setCondicion(ses.getCondicion().accept(this));
        ses.setBloqueSiEntonces(ses.getBloqueSiEntonces().accept(this));
        ses.setBloqueSino(ses.getBloqueSino().accept(this));
        return ses;
    }

    public Nodo transform(Cuando c) throws ExcepcionTransformer {
        c.setCondicion(c.getCondicion().accept(this));

        List<CasoCuando> casosCuando = new ArrayList<>();
        for (CasoCuando caso : c.getCasos()) {
            casosCuando.add((CasoCuando) caso.accept(this));
        }
        c.setCasos(casosCuando);

        c.setBloqueElse(c.getBloqueElse().accept(this));
        return c;
    }

    public Mientras transform(Mientras m) throws ExcepcionTransformer {
        m.setCondicion(m.getCondicion().accept(this));
        m.setBloqueSentencias(m.getBloqueSentencias().accept(this));
        return m;
    }

    public Nodo transform(Para p) throws ExcepcionTransformer {
        p.setIdent(p.getIdent().accept(this));
        p.setBloqueSentencias(p.getBloqueSentencias().accept(this));
        return p;
    }

    public Retorno transform(Retorno r) throws ExcepcionTransformer {
        r.setExpr(r.getExpresion().accept(this));
        return r;
    }

    public Salir transform(Salir s) {
        return s;
    }

    public Continuar transform(Continuar c) {
        return c;
    }
}
