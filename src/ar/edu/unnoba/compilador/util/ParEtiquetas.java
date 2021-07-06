package ar.edu.unnoba.compilador.util;

public class ParEtiquetas {
    private final String verdadero;
    private final String falso;
    private boolean invertidas;

    public ParEtiquetas(String verdadero, String falso) {
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

    public boolean getInvertidas() {
        return invertidas;
    }

    public void setInvertidas() {
        this.invertidas = true;
    }
}
