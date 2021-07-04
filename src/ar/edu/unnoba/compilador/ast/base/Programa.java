package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.expresiones.Cadena;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.util.*;

public class Programa extends Nodo {
    private Encabezado encabezado;
    private Bloque cuerpo;
    private Alcance alcance;
    private Map<String, DecFun> tablaFunciones;
    // Para generar código IR sólo si se usan las funciones
    private Set<String> funPredefUsadas;
    private List<Cadena> arrCadenas = new ArrayList<>();

    public Programa(String nombre, Encabezado encabezado, Bloque cuerpo) {
        super(nombre);
        this.cuerpo = cuerpo;
        this.encabezado = encabezado;
    }

    public Encabezado getEncabezado() {
        return this.encabezado;
    }

    public void setEncabezado(Encabezado encabezado) {
        this.encabezado = encabezado;
    }

    public Bloque getCuerpo() {
        return this.cuerpo;
    }

    public void setCuerpo(Bloque cuerpo) {
        this.cuerpo = cuerpo;
    }

    public Alcance getAlcance() {
        return alcance;
    }

    public void setAlcance(Alcance alcance) {
        this.alcance = alcance;
    }

    public Map<String, DecFun> getTablaFunciones() {
        return tablaFunciones;
    }

    public void setTablaFunciones(Map<String, DecFun> tablaFunciones) {
        this.tablaFunciones = tablaFunciones;
    }

    public Set<String> getFunPredefUsadas() {
        return funPredefUsadas;
    }

    public void setFunPredefUsadas(Set<String> funPredefUsadas) {
        this.funPredefUsadas = funPredefUsadas;
    }

    public List<Cadena> getArrCadenas() {
        return arrCadenas;
    }

    public void setArrCadenas(List<Cadena> arrCadenas) {
        this.arrCadenas = arrCadenas;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Programa accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
