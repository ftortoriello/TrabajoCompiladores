package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Resta;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.MayorIgual;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.MenorIgual;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Entero;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import java_cup.runtime.ComplexSymbolFactory.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Estructura while.
 *
 * Tiene un nodo "expresion" de tipo booleano (la condición) y un nodo "bloque" de sentencias.
 */
public class Mientras extends Sentencia {
    private Expresion condicion;
    private Bloque bloqueSentencias;
    /** Si se convirtió de FOR a WHILE */
    private final boolean eraFor;

    public Mientras(Expresion condicion, Bloque bloqueSentencias) {
        super("Bloque WHILE");
        this.condicion = condicion;
        bloqueSentencias.setNombre("Cuerpo WHILE");
        this.bloqueSentencias = bloqueSentencias;
        this.eraFor = false;
    }

    /** Construir WHILE a partir de FOR */
    private Mientras(Identificador ident, int valorIni, int valorFin, int salto, Bloque bloque) {
        super("Bloque WHILE");
        bloque.setNombre("Cuerpo WHILE");
        this.bloqueSentencias = bloque;
        this.eraFor = true;

        // Si aceptamos salto == 0 se producen bucles infinitos.
        // Podría verificarse en el lexer esto, pero si distinguimos enteros positivos de los que
        // incluyen 0 es un lío, porque hay que duplicar todas las reglas que usan LIT_ENTERO...
        if (salto == 0) {
            throw new IllegalStateException("No se permite que el iterador de FOR tenga salto 0.");
        }

        List<Nodo> sentencias = bloque.getSentencias();

        // Crear el nodo siempre positivo, porque si es negativo después se crea como resta
        // (Siempre viene positivo del parser)
        Entero litSalto = new Entero(salto);
        // Si el valor final es menor al inicial, dejar salto negativo, sino positivo
        if (valorIni > valorFin) salto = -salto;

        // Crear condición del WHILE según los valores del FOR
        Entero litValorFin = new Entero(valorFin);
        if (salto > 0) {
            this.condicion = new MenorIgual(ident, litValorFin);
        } else {
            this.condicion = new MayorIgual(ident, litValorFin);
        }

        // Añadir incremento o decremento del contador al final del bloque for
        Expresion inc;
        if (salto > 0) {
            inc = new Suma(ident, litSalto);
        } else {
            inc = new Resta(ident, litSalto);
        }

        Asignacion asigInc = new Asignacion(ident, inc);
        sentencias.add(asigInc);
    }

    /**
     * Retorna un bloque con dos sentencias: la asignación del valor inicial del contador, y el
     * cuerpo de la estructura FOR convertido en WHILE.
     */
    public static Bloque crearBloquePara(Identificador ident, int valorIni, int valorFin, int salto,
                                         Bloque bloque, Location posIzq, Location posDer) {
        List<Nodo> sentencias = new ArrayList<>();
        sentencias.add(new Asignacion(ident, new Entero(valorIni)));

        Mientras mientras = new Mientras(ident, valorIni, valorFin, salto, bloque);
        mientras.setPosicion(posIzq, posDer);
        sentencias.add(mientras);

        return new Bloque("Conversión\\nFOR a WHILE", sentencias);
    }

    /** crearBloquePara() usando como valor de salto predeterminado 1. */
    public static Bloque crearBloquePara(Identificador ident, int valorIni, int valorFin,
                                         Bloque bloque, Location posIzq, Location posDer) {
        return crearBloquePara(ident, valorIni, valorFin, 1, bloque, posIzq, posDer);
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public void setCondicion(Expresion condicion) {
        this.condicion = condicion;
    }

    public Bloque getBloqueSentencias() {
        return bloqueSentencias;
    }

    public void setBloqueSentencias(Bloque bloqueSentencias) {
        this.bloqueSentencias = bloqueSentencias;
    }

    public boolean eraFor() {
        return eraFor;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Mientras accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
