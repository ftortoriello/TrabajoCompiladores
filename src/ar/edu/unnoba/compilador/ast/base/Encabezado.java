package ar.edu.unnoba.compilador.ast.base;

import ar.edu.unnoba.compilador.ast.instrucciones.Sentencia;

import java.util.Collections;
import java.util.List;

public abstract class Encabezado extends Nodo {
    // Encabezado va a contener una lista de sentencias, la cual incluye las declaraciones de funciones
    // y variables globales que tienen están antes del bloque del programa principal.
    List<Sentencia> sentencias;

    // Encabezado definido
    public Encabezado(List<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    // Encabezado vacío
    public Encabezado() { this.sentencias = Collections.emptyList(); }
}
