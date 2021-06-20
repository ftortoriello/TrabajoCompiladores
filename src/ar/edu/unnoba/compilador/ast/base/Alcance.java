package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;

import java.util.TreeMap;

public class Alcance extends TreeMap<String, SimboloVariable> {
    private static final long serialVersionUID = 1L;

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

    public SimboloVariable resolver(String nombre) {
        Alcance alcanceActual = this;
        SimboloVariable s;
        while (alcanceActual != null) {
            s = alcanceActual.get(nombre);
            if (s != null) {
                return s;
            }
            // No se encontró, buscar en el alcance del padre
            alcanceActual = alcanceActual.getPadre();
        }
        // No se encontró en ningún alcance
        return null;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
