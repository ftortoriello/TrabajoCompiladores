package ar.edu.unnoba.compilador.util;

public class Etiquetas {
    private final String primera;
    private final String segunda;
    private String etiContinuar = null;
    private boolean saltoInvertido;

    /**
     * @param primera: Bloque THEN o WHILE
     * @param segunda: Fin de bloque THEN o WHILE
     */
    public Etiquetas(String primera, String segunda) {
        this.primera = primera;
        this.segunda = segunda;
        this.saltoInvertido = false;
    }

    public String getSaltoPrimera() {
        if (saltoInvertido) {
            return segunda;
        } else {
            return primera;
        }
    }

    public String getSaltoSegunda() {
        if (saltoInvertido) {
            return primera;
        } else {
            return segunda;
        }
    }

    public String getSaltoContinuar() {
        return etiContinuar;
    }

    public void setEtiContinuar(String etiContinuar) {
        this.etiContinuar = etiContinuar;
    }

    public void invertirSalto() {
        this.saltoInvertido = !this.saltoInvertido;
    }

    public boolean getSaltoInvertido() {
        return this.saltoInvertido;
    }
}
