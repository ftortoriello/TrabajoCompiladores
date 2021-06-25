package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.util.List;

public class Encabezado extends Nodo {
    private List<Declaracion> declaraciones;

    public Encabezado(List<Declaracion> declaraciones) {
        super(declaraciones.size() == 0 ? "Encabezado (vacío)" : "Encabezado");
        this.declaraciones = declaraciones;
    }

    public List<Declaracion> getDeclaraciones() {
        return declaraciones;
    }

    public void setDeclaraciones(List<Declaracion> declaraciones) {
        this.declaraciones = declaraciones;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Encabezado accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
