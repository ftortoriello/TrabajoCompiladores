package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.*;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;

import java.util.Map;

/* Transformer que asigna tipos a los identificadores y valida la
 * compatibilidad de tipos, haciendo conversiones implícitas si es necesario.
 * También reemplaza los identificadores que encuentra por los símbolos que se
 * encuentran en la tabla de símbolos.
 */
public class TransformerTipos extends Transformer {
    private Alcance alcanceActual;
    private Map<String, SimboloFuncion> tablaFunciones;

    // Métodos auxiliares

    private static Tipo getTipoEnComun(Tipo tipo1, Tipo tipo2) throws ExcepcionTransformer {
        if (tipo1 == tipo2) {
            return tipo1;
        }
        if (tipo1 == Tipo.INTEGER && tipo2 == Tipo.FLOAT) {
            return tipo2;
        }
        if (tipo1 == Tipo.FLOAT && tipo2 == Tipo.INTEGER) {
            return tipo1;
        }
        throw new ExcepcionTransformer(
                String.format("No existe un tipo común entre %s y %s", tipo1, tipo2));
    }

    private static Expresion convertirATipo(Expresion expresion, Tipo tipoDestino) throws ExcepcionTransformer {
        Tipo tipoOrigen = expresion.getTipo();
        if (tipoOrigen == tipoDestino) {
            return expresion;
        }
        if (tipoOrigen == Tipo.INTEGER && tipoDestino == Tipo.FLOAT) {
            System.out.printf("Advertencia: convirtiendo «%s» de integer a float%n", expresion);
            return new EnteroAFlotante(expresion);
        }
        if (tipoOrigen == Tipo.FLOAT && tipoDestino == Tipo.INTEGER) {
            System.out.printf("Advertencia: convirtiendo «%s» de float a integer%n", expresion);
            return new FlotanteAEntero(expresion);
        }
        throw new ExcepcionTransformer(
                String.format("No existe un tipo común entre %s y %s", tipoOrigen, tipoDestino));
    }

    // Retorna el símbolo si está en el alcance actual y se pudo cambiar el tipo
    private SimboloVariable cambiarTipoVariable(Valor v) {
        SimboloVariable s = alcanceActual.resolver(v.getNombre());
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

    private SimboloFuncion cambiarTipoFuncion(Valor v) {
        SimboloFuncion s = tablaFunciones.get(v.getNombre());
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
    private static Tipo transformOperacionBinaria(OperacionBinaria ob) throws ExcepcionTransformer {
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
    public Programa transform(Programa p) throws ExcepcionTransformer {
        alcanceActual = p.getAlcance();
        tablaFunciones = p.getTablaFunciones();
        p = super.transform(p);
        alcanceActual = null;
        return p;
    }

    @Override
    public DecVarIni transform(DecVarIni dvi) throws ExcepcionTransformer {
        dvi = super.transform(dvi);
        dvi.setExpresion(convertirATipo(dvi.getExpresion(), dvi.getIdent().getTipo()));
        return dvi;
    }

    @Override
    public DecFun transform(DecFun df) throws ExcepcionTransformer {
        alcanceActual = df.getAlcance();
        df = super.transform(df);
        alcanceActual = alcanceActual.getPadre();
        return df;
    }

    @Override
    public Bloque transform(Bloque b) throws ExcepcionTransformer {
        alcanceActual = b.getAlcance();
        b = super.transform(b);
        alcanceActual = alcanceActual.getPadre();
        return b;
    }

    @Override
    public Identificador transform(Identificador i) throws ExcepcionTransformer {
        i = super.transform(i);
        SimboloVariable s = cambiarTipoVariable(i);
        if (s == null) {
            throw new ExcepcionTransformer(String.format("No se pudo asignar un tipo a la variable «%s»", i.getNombre()));
        }
        // Reemplazar cada Identificador por el SimboloVariable correspondiente
        return s;
    }

    @Override
    public InvocacionFuncion transform(InvocacionFuncion i) throws ExcepcionTransformer {
        i = super.transform(i);

        // No es necesario buscar en el alcance las funciones predefinidas
        if (i.getEsPredefinida()) {
            return i;
        }

        SimboloFuncion s = cambiarTipoFuncion(i);
        if (s == null) {
            throw new ExcepcionTransformer(String.format("No se pudo asignar un tipo a la función «%s»", i.getNombre()));
        }

        DecFun decFun = s.getDeclaracion();
        int cantArgs = i.getArgs().size();

        // Validar el tipo de cada argumento, y convertir cuando sea necesario
        for (int iArg = 0; iArg < cantArgs; iArg++) {
            Expresion arg = i.getArgs().get(iArg);
            Tipo tipoOriginal = decFun.getParams().get(iArg).getTipo();

            // TODO acá tendría que modificar el SimboloFuncion
            //s.getDeclaracion().getArgs().set(iArg, convertirATipo(arg, tipoOriginal));
            i.getArgs().set(iArg, convertirATipo(arg, tipoOriginal));
        }
        return i;
    }

    @Override
    public Asignacion transform(Asignacion a) throws ExcepcionTransformer {
        a = super.transform(a);
        a.setExpresion(convertirATipo(a.getExpresion(), a.getIdent().getTipo()));
        return a;
    }

    @Override
    public Expresion transform(OperacionBinaria ob) throws ExcepcionTransformer {
        ob = (OperacionBinaria) super.transform(ob);
        Tipo tipoEnComun = transformOperacionBinaria(ob);
        ob.setTipo(tipoEnComun);
        return ob;
    }

    // Las relaciones son como las operaciones binarias, pero su tipo siempre es boolean
    @Override
    public Relacion transform(Relacion r) throws ExcepcionTransformer {
        r = (Relacion) super.transform(r);
        Tipo tipoEnComun = transformOperacionBinaria(r);
        // Sólo las relaciones de igualdad y desigualdad aceptan operandos booleanos
        if ((tipoEnComun == Tipo.BOOLEAN) && !(
                r instanceof Igualdad || r instanceof Desigualdad)) {
            throw new ExcepcionTransformer(String.format("No se puede realizar una comparación \"%s\" entre tipos boolean", r.getNombre()));
        }
        r.setTipo(Tipo.BOOLEAN);
        return r;
    }

    @Override
    public Expresion transform(OperacionUnaria ou) throws ExcepcionTransformer {
        ou = (OperacionUnaria) super.transform(ou);
        if (ou.getTipo() == Tipo.UNKNOWN) {
            ou.setTipo(ou.getExpresion().getTipo());
        } else {
            ou.setExpresion(convertirATipo(ou.getExpresion(), ou.getTipo()));
        }
        return ou;
    }

    @Override
    public Retorno transform(Retorno r) throws ExcepcionTransformer {
        // Transformar la expresión interna del return
        r = super.transform(r);
        // Y compararla con el de la función a la que pertenece
        DecFun ultFunVisitada = getUltFunVisitada();
        if (ultFunVisitada == null) {
            // No tendría que suceder si se ejecutó el Visitor de sentencias de
            // control
            return null;
        }
        r.setFun(ultFunVisitada);
        r.setExpr(convertirATipo(r.getExpresion(), ultFunVisitada.getTipo()));
        return r;
    }

    @Override
    public Mientras transform(Mientras m) throws ExcepcionTransformer {
        m = super.transform(m);
        if (m.getCondicion().getTipo() != Tipo.BOOLEAN) {
            throw new ExcepcionTransformer("El tipo de la condición de «while» no es boolean");
        }
        return m;
    }

    @Override
    public Sentencia transform(Para p) throws ExcepcionTransformer {
        p = (Para) super.transform(p);
        // No se puede convertir OperacionConversion a Identificador acá.
        // Para que soporte conversiones implícitas de flotante a entero
        // tendríamos que reemplazar en Para el Identificador por Expresion.
        if (p.getIdent().getTipo() != Tipo.INTEGER) {
            throw new ExcepcionTransformer("El tipo de la variable a iterar en «for» no es integer");
        }
        return p;
    }
}
