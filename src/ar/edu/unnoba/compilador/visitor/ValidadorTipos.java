package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Asignacion;

public class ValidadorTipos extends Transformer {
    private Alcance alcanceActual;

    private static Tipo getTipoEnComun(Tipo tipo1, Tipo tipo2) throws ExcepcionDeTipos {
        if (tipo1 == tipo2) {
            return tipo1;
        }
        if (tipo1 == Tipo.INTEGER && tipo2 == Tipo.FLOAT) {
            return tipo2;
        }
        if (tipo1 == Tipo.FLOAT && tipo2 == Tipo.INTEGER) {
            return tipo1;
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %1$s y %2$s\n", tipo1, tipo2));
    }

    private static Expresion convertirATipo(Expresion expresion, Tipo tipoDestino) throws ExcepcionDeTipos {
        Tipo tipoOrigen = expresion.getTipo();
        if (tipoOrigen == tipoDestino) {
            return expresion;
        }
        if (tipoOrigen == Tipo.INTEGER && tipoDestino == Tipo.FLOAT) {
            return new EnteroAFlotante(expresion);
        }
        if (tipoOrigen == Tipo.FLOAT && tipoDestino == Tipo.INTEGER) {
            return new FlotanteAEntero(expresion);
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %1$s y %2$s\n", tipoOrigen, tipoDestino));
    }


    // Transforms

    @Override
    public Programa transform(Programa p) throws ExcepcionDeTipos {
        super.transform(p);
        this.alcanceActual = p.getCuerpo().getAlcance();
        return p;
    }

    @Override
    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos {
        Asignacion asignacion = super.transform(a);
        asignacion.setExpresion(convertirATipo(asignacion.getExpresion(), asignacion.getIdent().getTipo()));
        return asignacion;
    }

    @Override
    public OperacionBinaria transform(OperacionBinaria ob) throws ExcepcionDeTipos {
        super.transform(ob);

        Expresion expIzquierda = ob.getIzquierda();
        Expresion expDerecha = ob.getDerecha();

        Tipo tipoEnComun = getTipoEnComun(expIzquierda.getTipo(), expDerecha.getTipo());
        expIzquierda = convertirATipo(expIzquierda, tipoEnComun);
        expDerecha = convertirATipo(expDerecha, tipoEnComun);

        ob.setIzquierda(expIzquierda);
        ob.setDerecha(expDerecha);
        ob.setTipo(tipoEnComun);
        return ob;
    }

    @Override
    public OperacionUnaria transform(OperacionUnaria ou) throws ExcepcionDeTipos {
        super.transform(ou);
        if (ou.getTipo() == Tipo.UNKNOWN) {
            ou.setTipo(ou.getExpresion().getTipo());
        } else {
            ou.setExpresion(convertirATipo(ou.getExpresion(), ou.getTipo()));
        }
        return ou;
    }

    @Override
    public Identificador transform(Identificador ident) throws ExcepcionDeTipos {
        Nodo elemento = alcanceActual.resolver(ident.getNombre());
        Tipo tipo = Tipo.UNKNOWN;
        /* TODO
        if (elemento instanceof Variable) {
            tipo = ((Variable) elemento).getTipo();
        }
        */
        if (tipo != Tipo.UNKNOWN) {
            ident.setTipo(tipo);
            return ident;
        }
        throw new ExcepcionDeTipos(String.format("No se declaró el nombre %1$s\n", ident.getNombre()));
    }
}
