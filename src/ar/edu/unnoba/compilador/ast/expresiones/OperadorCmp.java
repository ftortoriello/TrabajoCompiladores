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
            case MENOR_IGUAL:
                return "<=";
            default:
                throw new IllegalStateException("Tipo de comparación inesperada: " + op);
        }
    }

    // Recibe el lexema OP_COMPARACION (en forma de cadena) y devuelve el enum OperadorCmp correspondiente
    public static OperadorCmp getTipoOpCmp(String opStr) {
        OperadorCmp op;

        switch (opStr) {
            case "==":
                op = OperadorCmp.IGUALDAD;
                break;
            case "!=":
                op = OperadorCmp.DESIGUALDAD;
                break;
            case ">":
                op = OperadorCmp.MAYOR;
                break;
            case ">=":
                op = OperadorCmp.MAYOR_IGUAL;
                break;
            case "<":
                op = OperadorCmp.MENOR;
                break;
            case "<=":
                op = OperadorCmp.MENOR_IGUAL;
                break;
            default:
                throw new IllegalStateException("Operador relacional inesperado: " + opStr);
        }

        return op;

    }

}
