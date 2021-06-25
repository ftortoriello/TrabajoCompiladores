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
        switch (tipoStr.toUpperCase()) {
            case "BOOLEAN": return Tipo.BOOLEAN;
            case "INTEGER": return Tipo.INTEGER;
            case "FLOAT":   return Tipo.FLOAT;
            default:        return Tipo.UNKNOWN;
        }
    }

    /** Retorna el tipo usado en el lenguaje IR. */
    public String getIR() {
        switch (this) {
            case BOOLEAN: return "i1";
            case INTEGER: return "i32";
            // Usamos double para representar nros. con punto flotante, para perder menos precisión
            case FLOAT: return "double";
            default: throw new IllegalStateException(
                    String.format("getTipoIR(): Tipo %s inesperado", this));
        }
    }

    /** Retorna el String del valor predeterminado usado en IR. */
    public String getValorDefIR() {
        switch (this) {
            case BOOLEAN: return "false";
            case INTEGER: return "0";
            case FLOAT: return "0.0";
            default: throw new IllegalStateException(
                    String.format("getTipoIR(): Tipo %s inesperado", this));
        }
    }
}
