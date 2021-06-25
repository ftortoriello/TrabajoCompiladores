package ar.edu.unnoba.compilador.sintaxis;

import java_cup.runtime.ComplexSymbolFactory.Location;

public class Util {
    /** Obtener un String. Si izq y der son iguales, contiene con un valor, sino los dos. */
    private static String getStringRango(int izq, int der) {
        if (izq == der) {
            return String.format("%d", izq);
        } else {
            return String.format("%d-%d", izq, der);
        }
    }

    /** Dados dos objetos Location, obtener la posición legible del ComplexSymbol. */
    public static String getPosicion(Location izq, Location der) {
        if (izq == null || der == null) return "";

        return "Línea:" + getStringRango(izq.getLine(), der.getLine()) +
               " Columna:" + getStringRango(izq.getColumn(), der.getColumn());
    }
}
