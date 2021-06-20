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

    // Recibe el lexema TIPO (en forma de cadena) y devuelve el enum Tipo correspondiente
    public static Tipo getTipoDato(String tipoStr) {
        switch (tipoStr.toUpperCase()) {
            case "BOOLEAN": return Tipo.BOOLEAN;
            case "INTEGER": return Tipo.INTEGER;
            case "FLOAT":   return Tipo.FLOAT;
            default:        return Tipo.UNKNOWN;
        }
    }
}
