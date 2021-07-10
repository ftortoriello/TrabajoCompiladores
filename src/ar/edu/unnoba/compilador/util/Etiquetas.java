package ar.edu.unnoba.compilador.util;

public class Etiquetas {
    private final String primera;
    private final String segunda;
    private String etiIterador = null;
    private boolean invertidas;

    public Etiquetas(String primera, String segunda) {
        this.primera = primera;
        this.segunda = segunda;
        this.invertidas = false;
    }

    public String getPrimera() {
        if (estanInvertidas()) {
            return segunda;
        } else {
            return primera;
        }
    }

    public String getSegunda() {
        if (estanInvertidas()) {
            return primera;
        } else {
            return segunda;
        }
    }

    public String getEtiIterador() {
        return etiIterador;
    }

    public void setEtiIterador(String etiIterador) {
        this.etiIterador = etiIterador;
    }

    public boolean estanInvertidas() {
        return invertidas;
    }

    public void invertir() {
        this.invertidas = !this.invertidas;
    }
}
