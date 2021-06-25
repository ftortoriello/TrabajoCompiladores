package ar.edu.unnoba.compilador.excepciones;

import ar.edu.unnoba.compilador.util.Constantes;

public class GestorExcepciones {
    // Recibe Throwable para permitir mostrar Exception y Error
    public static void mostrar(Throwable e) {
        if (Constantes.mostrarExcepcionesDetalladas) {
            e.printStackTrace(System.err);
        } else {
            // Mostrar sólo la descripción
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
