package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Simbolo;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Valor;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;

import java.util.Locale;

/* Transformer que asigna tipos a los identificadores y valida la
 * compatibilidad de tipos, haciendo conversiones implícitas si es necesario.
 * TODO: y reemplaza Id por Simbolo
 * TODO: Verificar tipos en DecVarInicializada
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
            System.out.println(String.format("Advertencia: convirtiendo %s de entero a flotante", expresion));
            return new EnteroAFlotante(expresion);
        }
        if (tipoOrigen == Tipo.FLOAT && tipoDestino == Tipo.INTEGER) {
            System.out.println(String.format("Advertencia: convirtiendo %s de flotante a entero", expresion));
            return new FlotanteAEntero(expresion);
        }
        throw new ExcepcionDeTipos(
                String.format("No existe un tipo común entre %s y %s", tipoOrigen, tipoDestino));
    }

    // Retorna si se encontró el símbolo en el alcance actual y se cambió el tipo
    private boolean cambiarTipo(Valor v) {
        Simbolo s = alcanceActual.resolver(v.getNombre());
        if (s == null) return false;
        Tipo tipo = s.getTipo();
        if (tipo == Tipo.UNKNOWN) return false;
        v.setTipo(tipo);
        return true;
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
    public Bloque transform(Bloque b) throws ExcepcionDeTipos {
        alcanceActual = b.getAlcance();
        b = super.transform(b);
        alcanceActual = alcanceActual.getPadre();
        return b;
    }

    @Override
    public Identificador transform(Identificador i) throws ExcepcionDeTipos {
        if (!cambiarTipo(i)) {
            throw new ExcepcionDeTipos(String.format("No se declaró la variable %s", i.getNombre()));
        }
        return super.transform(i);
    }

    @Override
    public InvocacionFuncion transform(InvocacionFuncion i) throws ExcepcionDeTipos {
        // TODO: Alguna manera mejor de manejar esto?
        // Se podrían poner en el alcance global... (no se podrían sobreescribir)
        // O que en el parser ponga un parámetro esPrededifinida, y si es así la ignore acá...

        // No buscar en el alcance las funciones predefinidas
        switch (i.getNombre().toUpperCase()) {
            case "WRITE":
            case "WRITELN":
                break;

            // Establecer tipo de las funciones "read"
            case "READ_INTEGER":
                i.setTipo(Tipo.INTEGER);
                break;
            case "READ_FLOAT":
                i.setTipo(Tipo.FLOAT);
                break;
            case "READ_BOOLEAN":
                i.setTipo(Tipo.BOOLEAN);
                break;
            default:
                if (!cambiarTipo(i)) {
                    throw new ExcepcionDeTipos(String.format("No se definió la función %s", i.getNombre()));
                }
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
}
