package ar.edu.unnoba.compilador.ast.base;

import java.util.HashMap;

public class Alcance extends HashMap<String, Nodo> {
    // El "nombre" del alcance puede ser "global", el nombre de la función en la que está incluido, etc...
    private String nombre;
    // Necesitamos el padre porque si una variable no está definida en este ámbito le preguntamos a él
    private Alcance padre;

    public Alcance(String nombre) {
        // Alcance global (no tiene padre)
        this.nombre = nombre;
        this.padre = null;
    }

    public Alcance(String nombre, Alcance padre) {
        // Todos los demás alcances (de función, bloque, ...)
        this.nombre = nombre;
        this.padre = padre;
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

    public Nodo resolver(String nombre) {
        Alcance alcanceActual = this;
        Nodo elemento = null;
        while (alcanceActual != null) {
            elemento = alcanceActual.get(nombre);
            if (elemento != null) {
                return elemento;
            }
            alcanceActual = alcanceActual.getPadre();
        }
        // No se encontró
        return null;
    }
}
