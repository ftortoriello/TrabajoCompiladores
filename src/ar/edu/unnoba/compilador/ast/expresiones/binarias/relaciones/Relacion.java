package ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.OperadorCmp;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

public abstract class Relacion extends OperacionBinaria {

    public Relacion(Expresion izquierda, Expresion derecha, String nombre) {
        super(izquierda, derecha, nombre);
    }

    @Override
    public Expresion accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    @Override
    public Expresion evaluar() {
        if (!(getIzquierda().evaluar() instanceof Literal) || !(getDerecha().evaluar() instanceof Literal)) {
            return this;
        }

        float litIzq = Float.parseFloat(((Literal) getIzquierda().evaluar()).getValor());
        float litDer = Float.parseFloat(((Literal) getDerecha().evaluar()).getValor());
        boolean resultado = calcularResultado(litIzq, litDer);

        return new Literal(String.valueOf(resultado), Tipo.BOOLEAN, "Literal BOOLEAN");
    }

    private Boolean calcularResultado(Float litIzq, Float litDer) {
        switch (getNombre()) {
            case "!=":  return litIzq != litDer;
            case "==":  return litIzq.equals(litDer);
            case ">":   return litIzq > litDer;
            case ">=":  return litIzq >= litDer;
            case "<":   return litIzq < litDer;
            case "<=":  return litIzq <= litDer;
            default:
                throw new IllegalStateException("Operador inesperado al aplicar Constant Folding: " + getNombre());
        }
    }

    // Crea una clase según sea el operador utilizado
    // Si se utiliza el operador de IGUALDAD, devuelvo una clase Igualdad, etc.
    public static Relacion getClaseRel(OperadorCmp op, Expresion expr1, Expresion expr2) {
        Relacion comp;

        switch (op) {
            case IGUALDAD:
                comp = new Igualdad(expr1, expr2);
                break;
            case DESIGUALDAD:
                comp = new Desigualdad(expr1, expr2);
                break;
            case MAYOR:
                comp = new Mayor(expr1, expr2);
                break;
            case MAYOR_IGUAL:
                comp = new MayorIgual(expr1, expr2);
                break;
            case MENOR:
                comp = new Menor(expr1, expr2);
                break;
            case MENOR_IGUAL:
                comp = new MenorIgual(expr1, expr2);
                break;
            default:
                throw new IllegalStateException("Operador inesperado: " + op);
        }

        return comp;
    }

    @Override
    public String getInstruccionIR() {
        // TODO las comparaciones hay que hacerlas a mano
        return null;
    }
}
