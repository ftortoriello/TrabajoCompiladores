package ar.edu.unnoba.compilador.ast.expresiones;

public enum OperadorCmp {
    IGUALDAD,
    DESIGUALDAD,
    MAYOR,
    MAYOR_IGUAL,
    MENOR,
    MENOR_IGUAL;

    public static String getStrOp(OperadorCmp op) {
        switch (op) {
            case IGUALDAD:    return "==";
            case DESIGUALDAD: return "!=";
            case MAYOR:       return ">";
            case MAYOR_IGUAL: return ">=";
            case MENOR:       return "<";
            case MENOR_IGUAL: return "<=";
            default: throw new IllegalStateException("Tipo de comparación inesperada: " + op);
        }
    }

    /**
     * Recibe el lexema OP_COMPARACION (en forma de cadena) y devuelve el enum OperadorCmp
     * correspondiente.
     */
    public static OperadorCmp getTipoOpCmp(String opStr) {
        switch (opStr) {
            case "==": return OperadorCmp.IGUALDAD;
            case "!=": return OperadorCmp.DESIGUALDAD;
            case ">":  return OperadorCmp.MAYOR;
            case ">=": return OperadorCmp.MAYOR_IGUAL;
            case "<":  return OperadorCmp.MENOR;
            case "<=": return OperadorCmp.MENOR_IGUAL;
            default: throw new IllegalStateException("Operador relacional inesperado: " + opStr);
        }
    }
}
