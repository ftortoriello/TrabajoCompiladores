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
            case IGUALDAD:
                return "==";
            case DESIGUALDAD:
                return "!=";
            case MAYOR:
                return ">";
            case MAYOR_IGUAL:
                return ">=";
            case MENOR:
                return "<";
            default:
                return "<=";
        }
    }
}
