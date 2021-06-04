package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

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
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public Encabezado accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }
}
