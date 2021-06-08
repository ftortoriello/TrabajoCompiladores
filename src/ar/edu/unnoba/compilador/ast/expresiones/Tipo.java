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
        Tipo tipo;

        switch (tipoStr.toUpperCase()) {
            case "BOOLEAN":
                tipo = Tipo.BOOLEAN;
                break;
            case "INTEGER":
                tipo = Tipo.INTEGER;
                break;
            case "FLOAT":
                tipo = Tipo.FLOAT;
                break;
            default:
                tipo = Tipo.UNKNOWN;
                break;
        }

        return tipo;
    }
}
