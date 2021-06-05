package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.*;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Simbolo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Valor;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;

/* Transformer que asigna tipos a los identificadores y valida la
 * compatibilidad de tipos, haciendo conversiones implícitas si es necesario.
 * TODO: Reemplazar Identificador por Simbolo
 * TODO: Verificar tipos en DecVarInicializada
 * TODO: Transformar DecVar a DecVarInicializada con los valores por defecto
 */
public class TransformerTipos extends Transformer {
    private Alcance alcanceActual;

    // Métodos auxiliares

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
                String.format("No existe un tipo común entre %s y %s", tipo1, tipo2));
    }

    private static Expresion convertirATipo(Expresion expresion, Tipo tipoDestino) throws ExcepcionDeTipos {
        Tipo tipoOrigen = expresion.getTipo();
        if (tipoOrigen == tipoDestino) {
            return expresion;
        }
        if (tipoOrigen == Tipo.INTEGER && tipoDestino == Tipo.FLOAT) {
            System.out.println(String.format("Advertencia: convirtiendo «%s» de entero a flotante", expresion));
            return new EnteroAFlotante(expresion);
        }
        if (tipoOrigen == Tipo.FLOAT && tipoDestino == Tipo.INTEGER) {
            System.out.println(String.format("Advertencia: convirtiendo «%s» de flotante a entero", expresion));
            return new FlotanteAEntero(expresion);
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %s y %s", tipoOrigen, tipoDestino));
    }

    // Retorna el símbolo, si está en el alcance actual y se pudo cambiar el tipo
    private Simbolo cambiarTipo(Valor v) {
        Simbolo s = alcanceActual.resolver(v.getNombre());
        if (s == null) {
            return null;
        }
        Tipo tipo = s.getTipo();
        if (tipo == Tipo.UNKNOWN) {
            // No se pudo cambiar el tipo
            return null;
        }
        v.setTipo(tipo);
        return s;
    }

    // Retorna el tipo en común
    private Tipo transformOperacionBinaria(OperacionBinaria ob) throws ExcepcionDeTipos {
        Expresion expIzquierda = ob.getIzquierda();
        Expresion expDerecha = ob.getDerecha();

        Tipo tipoEnComun = getTipoEnComun(expIzquierda.getTipo(), expDerecha.getTipo());
        expIzquierda = convertirATipo(expIzquierda, tipoEnComun);
        expDerecha = convertirATipo(expDerecha, tipoEnComun);

        ob.setIzquierda(expIzquierda);
        ob.setDerecha(expDerecha);

        return tipoEnComun;
    }

    // Transforms

    @Override
    public Programa transform(Programa p) throws ExcepcionDeTipos {
        alcanceActual = p.getAlcance();
        p = super.transform(p);
        alcanceActual = null;
        return p;
    }

    @Override
    public DecFuncion transform(DecFuncion df) throws ExcepcionDeTipos {
        alcanceActual = df.getAlcance();
        df = super.transform(df);
        alcanceActual = alcanceActual.getPadre();
        return df;
    }

    @Override
    public Bloque transform(Bloque b) throws ExcepcionDeTipos {
        alcanceActual = b.getAlcance();
        b = super.transform(b);
        alcanceActual = alcanceActual.getPadre();
        return b;
    }

    @Override
    public Identificador transform(Identificador i) throws ExcepcionDeTipos {
        if (cambiarTipo(i)== null) {
            throw new ExcepcionDeTipos(String.format("No se pudo asignar un tipo a la variable «%s»", i.getNombre()));
        }
        return super.transform(i);
    }

    @Override
    public InvocacionFuncion transform(InvocacionFuncion i) throws ExcepcionDeTipos {
        // TODO: validar posición, tipo y cantidad de parámetros
        // No buscar en el alcance las funciones predefinidas
        if (!i.getEsPredefinida() && (cambiarTipo(i) == null)) {
            throw new ExcepcionDeTipos(String.format("No se pudo asignar un tipo a la función «%s»", i.getNombre()));
        }
        return super.transform(i);
    }

    @Override
    public Asignacion transform(Asignacion a) throws ExcepcionDeTipos {
        Asignacion asignacion = super.transform(a);
        asignacion.setExpresion(convertirATipo(asignacion.getExpresion(), asignacion.getIdent().getTipo()));
        return asignacion;
    }

    @Override
    public OperacionBinaria transform(OperacionBinaria ob) throws ExcepcionDeTipos {
        ob = super.transform(ob);
        Tipo tipoEnComun = transformOperacionBinaria(ob);
        ob.setTipo(tipoEnComun);
        return ob;
    }

    // Las relaciones son como las operaciones binarias, pero su tipo siempre es boolean
    @Override
    public Relacion transform(Relacion r) throws ExcepcionDeTipos {
        r = super.transform(r);
        Tipo tipoEnComun = transformOperacionBinaria(r);
        // Sólo las relaciones de igualdad y desigualdad aceptan operandos booleanos
        if ((tipoEnComun == Tipo.BOOLEAN) && !(
                r instanceof Igualdad || r instanceof Desigualdad)) {
            throw new ExcepcionDeTipos(String.format("No se puede realizar una comparación \"%s\" entre tipos BOOLEAN", r.getNombre()));
        }
        r.setTipo(Tipo.BOOLEAN);
        return r;
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
    public Retorno transform(Retorno r) throws ExcepcionDeTipos {
        // Transformar la expresión interna del return
        super.transform(r);
        // Y compararla con el de la función a la que pertenece
        DecFuncion ultFunVisitada = getUltFunVisitada();
        if (ultFunVisitada == null) {
            // No tendría que suceder si se ejecutó el Visitor de sentencias de
            // control
            return null;
        }
        Expresion e = r.getExpr();
        Tipo tipoDeLaFuncion = ultFunVisitada.getTipo();
        if (e.getTipo() != tipoDeLaFuncion) {
            r.setExpr(convertirATipo(r.getExpr(), tipoDeLaFuncion));
        }
        return r;
    }
}
