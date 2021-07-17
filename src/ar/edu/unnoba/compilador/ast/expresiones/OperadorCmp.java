package ar.edu.unnoba.compilador.ast.expresiones;

public enum OperadorCmp {
    IGUALDAD,
    DESIGUALDAD,
    MAYOR,
    MAYOR_IGUAL,
    MENOR,
    MENOR_IGUAL;

    public static String getStrOp(OperadorCmp op) {
        return switch (op) {
            case IGUALDAD    -> "==";
            case DESIGUALDAD -> "!=";
            case MAYOR       -> ">";
            case MAYOR_IGUAL -> ">=";
            case MENOR       -> "<";
            case MENOR_IGUAL -> "<=";
            default -> throw new IllegalStateException("Tipo de comparación inesperada: " + op);
        };
    }

    /**
     * Recibe el lexema OP_COMPARACION (en forma de cadena) y devuelve el enum OperadorCmp
     * correspondiente.
     */
    public static OperadorCmp getTipoOpCmp(String opStr) {
        return switch (opStr) {
            case "==" -> OperadorCmp.IGUALDAD;
            case "!=" -> OperadorCmp.DESIGUALDAD;
            case ">"  -> OperadorCmp.MAYOR;
            case ">=" -> OperadorCmp.MAYOR_IGUAL;
            case "<"  -> OperadorCmp.MENOR;
            case "<=" -> OperadorCmp.MENOR_IGUAL;
            default -> throw new IllegalStateException("Operador relacional inesperado: " + opStr);
        };
    }
}
