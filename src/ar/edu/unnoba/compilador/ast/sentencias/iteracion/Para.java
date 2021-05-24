package ar.edu.unnoba.compilador.ast.sentencias.iteracion;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Identificador;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.visitor.Transformer;
import ar.edu.unnoba.compilador.visitor.Visitor;

public class Para extends Sentencia {
    // Tiene un nodo "identificador", un nodo numérico "valorInicial", un nodo numérico
    // "valorFinal", estos tres de tipo ENTERO. Luego tiene un nodo "bloque" de sentencias.
    // TODO: Si valorFinal < valorInicial, se invierte la lógica y se decrementa valorInicial.
    // TODO: asegurarse que Identificador ya esté definido y sea de tipo Entero.
    // Si valorInicial no está definido, su valor por defecto es 1.

    Identificador ident;
    int valorInicial, valorFinal;
    Bloque bloqueSentencias;
    int salto;

    public Para(String nombre, Identificador ident, int valorInicial, int valorFinal, int salto, Bloque bloqueSentencias) {
        super("Bloque FOR");
        this.ident = ident;
        this.valorInicial = valorInicial;
        this.valorFinal = valorFinal;
        this.salto = salto;
        this.bloqueSentencias = bloqueSentencias;
    }

    public Identificador getIdent() {
        return ident;
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

    public int getSalto() {
        return salto;
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s(VAR.: %s, V. INI.:%s, V. FIN.: %s, SALTO: %s)",
                this.getClass().getSimpleName(), this.getIdent().getEtiqueta(),
                this.getValorInicial(), this.getValorFinal(), this.getSalto());
    }

    @Override
    public <T> T accept(Visitor<T> v) throws ExcepcionDeAlcance {
        return null;
    }

    @Override
    public <R> R accept_transfomer(Transformer t) throws ExcepcionDeTipos {
        return null;
    }
}
