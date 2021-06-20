package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

/* Tiene un nodo "Identificador", nodos de tipo ENTERO "valorInicial" y
 * "valorFinal", y un nodo "Bloque" de sentencias.
 * El valor por defecto de "valorInicial" es 1.
 */

public class Para extends Sentencia {
    private Identificador ident;
    private final int valorInicial, valorFinal;
    private Bloque bloqueSentencias;
    private final int salto;

    public Para(Identificador ident, int valorInicial, int valorFinal, int salto, Bloque bloqueSentencias) {
        super("Bloque FOR");
        this.ident = ident;
        this.valorInicial = valorInicial;
        this.valorFinal = valorFinal;

        if (valorInicial <= valorFinal) {
            this.salto = Math.abs(salto);
        } else {
            // Si valorFinal < valorInicial, dejar salto negativo.
            this.salto = -Math.abs(salto);
        }

        bloqueSentencias.setNombre("Cuerpo\nFOR");
        this.bloqueSentencias = bloqueSentencias;
    }

    public Identificador getIdent() {
        return ident;
    }

    public void setIdent(Identificador ident) {
        this.ident = ident;
    }

    public int getValorInicial() {
        return valorInicial;
    }

    public int getValorFinal() {
        return valorFinal;
    }

    public Bloque getBloqueSentencias() {
        return bloqueSentencias;
    }

    public void setBloqueSentencias(Bloque bloque) {
        this.bloqueSentencias = bloque;
    }

    public int getSalto() {
        return salto;
    }

    @Override
    public String getEtiqueta() {
        return String.format("Bloque FOR\n(%s: %s => %s, salto: %s)",
                this.getIdent().getNombre(), this.getValorInicial(), this.getValorFinal(), this.getSalto());
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public Nodo accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }
}
