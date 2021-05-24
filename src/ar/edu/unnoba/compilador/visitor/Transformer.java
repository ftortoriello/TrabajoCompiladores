package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayList;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.*;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.*;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas.Division;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas.Multiplicacion;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas.Resta;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.operaciones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;


public abstract class Transformer {

    public Programa transform(Programa p) throws ExcepcionDeTipos {
        // p.setCuerpo(p.getCuerpo().accept_transfomer(this));
        return p;
    }

    public Identificador transform(Identificador i) throws ExcepcionDeTipos{
        return i;
    }

    public Variable transform(Variable v) {
        return v;
    }

    public Constante transform(Constante c) {
        return c;
    }

    public Bloque transform(Bloque b) throws ExcepcionDeTipos {
        ArrayList<Nodo> result = new ArrayList<>();
        for (Nodo sentencia : b.getSentencias()){
            result.add(sentencia.accept_transfomer(this));
        }
        b.setSentencias(result);
        return b;
    }

    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos{
        Identificador id = a.getIdentificador().accept_transfomer(this);
        Expresion e = a.getExpresion().accept_transfomer(this);
        a.setIdentificador(id);
        a.setExpresion(e);
        return a;
    }

    public DecVar transform(DecVar dv) {
        return dv;
    }

    private OperacionBinaria transformar_operacion_binaria(OperacionBinaria operacion) throws ExcepcionDeTipos{
        operacion.setIzquierda(operacion.getIzquierda().accept_transfomer(this));
        operacion.setDerecha(operacion.getDerecha().accept_transfomer(this));
        return operacion;
    }

    public Division transform(Division d) throws ExcepcionDeTipos {
        return (Division) transformar_operacion_binaria(d);
    }

    public Multiplicacion transform(Multiplicacion m) throws ExcepcionDeTipos {
        return (Multiplicacion) transformar_operacion_binaria(m);
    }

    public Resta transform(Resta r) throws ExcepcionDeTipos {
        return (Resta) transformar_operacion_binaria(r);
    }

    public Suma transform(Suma s) throws ExcepcionDeTipos {
        return (Suma) transformar_operacion_binaria(s);
    }

    public FlotanteAEntero transform(FlotanteAEntero fae) throws ExcepcionDeTipos {
        fae.setExpresion(fae.getExpresion().accept_transfomer(this));
        return fae;
    }

    public EnteroAFlotante transform(EnteroAFlotante eaf) throws ExcepcionDeTipos {
        eaf.setExpresion(eaf.getExpresion().accept_transfomer(this));
        return eaf;
    }
}
