package ar.edu.unnoba.compilador.ast.expresiones;

public enum Tipo {
    BOOLEAN,
    INTEGER,
    FLOAT,
    UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
