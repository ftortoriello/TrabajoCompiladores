package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
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
        String args = argumentos.toString();
        args = args.substring(1, args.length() - 1);    // sacar corchetes al principio y final
        String eti = String.format("%s(%s)", nombre, args);

        if (nombre.equals("write") || nombre.equals("writeln")) {
            // No poner el tipo a las funciones write() y writeln(), porque no que devuelven un valor
            return eti;
        } else {
            return String.format("%s\\n<%s>", eti, getTipo());
        }
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
