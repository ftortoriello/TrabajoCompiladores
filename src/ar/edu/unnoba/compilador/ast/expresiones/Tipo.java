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

    /** Recibe el lexema TIPO (en forma de cadena) y devuelve el enum Tipo correspondiente. */
    public static Tipo getTipoDato(String tipoStr) {
        return switch (tipoStr.toUpperCase()) {
            case "BOOLEAN" -> Tipo.BOOLEAN;
            case "INTEGER" -> Tipo.INTEGER;
            case "FLOAT"   -> Tipo.FLOAT;
            default        -> Tipo.UNKNOWN;
        };
    }

    /** Retorna el tipo usado en el lenguaje IR. */
    public String getIR() {
        return switch (this) {
            case BOOLEAN -> "i1";
            case INTEGER -> "i32";
            // Usamos double para representar nros. con punto flotante, para perder menos precisión
            case FLOAT   -> "double";
            default -> throw new IllegalStateException(
                    String.format("getTipoIR(): Tipo %s inesperado", this));
        };
    }

    /** Retorna el String del valor predeterminado usado en IR. */
    public String getValorDef() {
        return switch (this) {
            case BOOLEAN -> "false";
            case INTEGER -> "0";
            case FLOAT   -> "0.0";
            default -> throw new IllegalStateException(
                    String.format("getTipoIR(): Tipo %s inesperado", this));
        };
    }
}
