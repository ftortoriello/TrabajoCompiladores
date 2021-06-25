package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

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

    public List<Expresion> getArgs() {
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
        String nombre = getNombre();
        String args = argumentos.toString()
                .replace("[", "")
                .replace("]", "");

        if (!(nombre.equals("write") || nombre.equals("writeln"))) {
            return String.format("%s(%s)\\n<%s>", nombre, args, getTipo());
        }

        /* Es una invocación a write o writeln.
         * Fijarse si su argumento es una cadena literal (podría ser una expresión).
         * En ese caso, acomodar las cadenas para que el gráfico las muestre idénticas al código de
         * entrada y no quede el DOT con errores de sintaxis.
         */
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

        // write y writeln no son funciones que devuelven un valor; no poner el tipo
        return String.format("%s(%s)", nombre, args);
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public InvocacionFuncion accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }

    public Expresion evaluar() {
        return this;
    }
}
