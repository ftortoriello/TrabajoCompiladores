package ar.edu.unnoba.compilador.util;

public class Etiquetas {
    private final String verdadero;
    private final String falso;
    private String etiIterador = null;
    private boolean invertidas;

    public Etiquetas(String verdadero, String falso) {
        this.verdadero = verdadero;
        this.falso = falso;
        this.invertidas = false;
    }

    public String getVerdadero() {
        return verdadero;
    }

    public String getFalso() {
        return falso;
    }

    public String getEtiIterador() {
        return etiIterador;
    }

    public void setEtiIterador(String etiIterador) {
        this.etiIterador = etiIterador;
    }

    public boolean getInvertidas() {
        return invertidas;
    }

    public void setInvertidas() {
        this.invertidas = true;
    }
}
