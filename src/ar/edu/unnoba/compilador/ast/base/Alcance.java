package ar.edu.unnoba.compilador.ast.base;

import java.util.HashMap;

public class Alcance extends HashMap {

    private String nombre;
    private Alcance padre;

    public Alcance(String nombre, Alcance padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public Alcance(String nombre) {
        this.nombre = nombre;
        this.padre = null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Alcance getPadre() {
        return padre;
    }

    public void setPadre(Alcance padre) {
        this.padre = padre;
    }

    public Object resolver(String name){
        Alcance alcance_actual = this;
        Object elemento = null;
        while(alcance_actual != null){
            elemento = alcance_actual.get(name);
            if(elemento != null){
                return elemento;
            }
            alcance_actual = alcance_actual.getPadre();
        }
        return elemento;
    }

}
