package ar.edu.unnoba.compilador.visitor;

import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.DecVar;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;

public class GeneradorAlcances extends Visitor<Void> {

    private Alcance alcance_actual;
    private Alcance alcance_global;


    public void procesar(Programa programa) throws ExcepcionDeAlcance{
        programa.getCuerpo().setAlcance(new Alcance("global"));
        alcance_global = alcance_actual = programa.getCuerpo().getAlcance();
        this.visit(programa);
    }

    private Object agregarSimbolo(String nombre, Object s){
        return this.alcance_actual.putIfAbsent(nombre, s);
    }

    @Override
    public Void visit(DecVar dv) throws ExcepcionDeAlcance {
        Variable var = new Variable(dv);
        Object result = this.agregarSimbolo(var.getDeclaracion().getId().getNombre(), dv);
        if(result!=null){
            throw new ExcepcionDeAlcance(
                    String.format("El nombre de la variable %1$s de tipo %2$s fue utilizado previamente\"]\n",
                            dv.getId().getNombre(), dv.getTipo() ));
        }
        return null;
    }

    @Override
    protected Void procesarBloque(Bloque bloque, List<Void> sentencias) {
        return null;
    }

    @Override
    protected Void procesarOperacionBinaria(OperacionBinaria ob, Void ei, Void ed) {
        return null;
    }

    @Override
    protected Void procesarNodo(Nodo n) {
        return null;
    }

    @Override
    protected Void procesarAsignacion(Asignacion a, Void identificador, Void expresion) {
        return null;
    }
}
