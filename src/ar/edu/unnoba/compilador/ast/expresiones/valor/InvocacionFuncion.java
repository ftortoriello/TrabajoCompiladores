package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public class InvocacionFuncion extends Valor {
    private List<Expresion> argumentos;
    private Boolean esPredefinida = true;

    public InvocacionFuncion(String nombre, List<Expresion> argumentos, Tipo tipo, Boolean esPredefinida) {
        super(tipo, nombre);
        this.argumentos = argumentos;
        this.esPredefinida = esPredefinida;
    }

    public InvocacionFuncion(String nombre, List<Expresion> argumentos, Tipo tipo) {
        super(tipo, nombre);
        this.argumentos = argumentos;
    }

    public InvocacionFuncion(String nombre, Tipo tipo) {
        super(tipo, nombre);
        this.argumentos = new ArrayList<>();
    }

    public List<Expresion> getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(List<Expresion> argumentos) {
        this.argumentos = argumentos;
    }

    public Boolean getEsPredefinida() {
        return esPredefinida;
    }

    @Override
    public String getEtiqueta() {
        String args = argumentos.toString()
                .replace("[", "")
                .replace("]", "");

        // Esto es un lío, pero no se como puede mejorar... Estaría bueno sacarlo de alguna manera como viene de la entrada
        // Acomodar las cadenas para que no queden errores de sintaxis en el DOT.
        // En el gráfico quedan de forma idéntica al código de entrada.

        // Si comienza con \" tiene sólo un argumento, que es una cadena literal.
        if (args.startsWith("\\\"")) {
            args = "\\\"" + args
                    // Sacar comillas externas escapadas
                    .substring(2, args.length() - 2)
                    // Mostrar escape en los caracteres de escape
                    .replace("\\", "\\\\\\\\")
                    .replace("\t", "\\\\t")
                    .replace("\n", "\\\\n")
                    .replace("\r", "\\\\r")
                    .replace("\"", "\\\\\\\"")
                    + "\\\"";
        }

        return String.format("%s(%s)\n<%s>", getNombre(), args, getTipo());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return v.visit(this);
    }

    @Override
    public InvocacionFuncion accept(Transformer t) throws ExcepcionDeTipos {
        return t.transform(this);
    }

    public Expresion evaluar() {
        return this;
    }
}
