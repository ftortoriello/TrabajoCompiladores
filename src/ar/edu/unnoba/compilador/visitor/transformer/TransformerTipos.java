package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Desigualdad;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Igualdad;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.EnteroAFlotante;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.FlotanteAEntero;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.ParamDef;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

import java.util.Map;

/**
 * Transformer que asigna tipos a los identificadores y valida la compatibilidad de tipos, haciendo
 * conversiones implícitas si es necesario.
 * También reemplaza los identificadores que encuentra por los símbolos que se encuentran en la
 * tabla de símbolos.
 */
public class TransformerTipos extends Transformer {
    private Alcance alcanceActual;
    private Map<String, DecFun> tablaFunciones;

    /**
     * Última función visitada. La guardamos para validar el tipo de retorno, y convertir el tipo
     * de la expresión return en caso de ser necesario.
     */
    private DecFun ultFunVisitada;

    public Alcance getAlcanceActual() {
        return alcanceActual;
    }

    public void setAlcanceActual(Alcance alcanceActual) {
        this.alcanceActual = alcanceActual;
    }

    public Map<String, DecFun> getTablaFunciones() {
        return tablaFunciones;
    }

    public void setTablaFunciones(Map<String, DecFun> tablaFunciones) {
        this.tablaFunciones = tablaFunciones;
    }

    protected DecFun getUltFunVisitada() {
        return ultFunVisitada;
    }

    protected void setUltFunVisitada(DecFun ultFunVisitada) {
        this.ultFunVisitada = ultFunVisitada;
    }

    // *** Métodos auxiliares ***

    private static Tipo getTipoEnComun(OperacionBinaria ob) throws ExcepcionTransformer {
        Tipo tipo1 = ob.getIzquierda().getTipo();
        Tipo tipo2 = ob.getDerecha().getTipo();

        if (tipo1 == tipo2) {
            return tipo1;
        }
        if (tipo1 == Tipo.INTEGER && tipo2 == Tipo.FLOAT) {
            return tipo2;
        }
        if (tipo1 == Tipo.FLOAT && tipo2 == Tipo.INTEGER) {
            return tipo1;
        }
        throw new ExcepcionTransformer(ob,
                String.format("No existe un tipo común entre %s y %s", tipo1, tipo2));
    }

    /** Si es necesario, reemplaza el nodo Expresion original por uno de OperacionConversion
     *  Por ej., si se quiere convertir de entero a flotante, el nodo Expresion va a quedar
     *  debajo de un nodo EnteroAFlotante. */
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
        throw new ExcepcionTransformer(expresion,
                String.format("No existe un tipo común entre %s y %s", tipoOrigen, tipoDestino));
    }

    /** Transforma el término izquierdo y derecho de la op. bin. al tipo común, y lo retorna. */
    private static Tipo transformOperacionBinaria(OperacionBinaria ob) throws ExcepcionTransformer {
        Expresion expIzquierda = ob.getIzquierda();
        Expresion expDerecha = ob.getDerecha();

        Tipo tipoEnComun = getTipoEnComun(ob);
        expIzquierda = convertirATipo(expIzquierda, tipoEnComun);
        expDerecha = convertirATipo(expDerecha, tipoEnComun);

        ob.setIzquierda(expIzquierda);
        ob.setDerecha(expDerecha);

        return tipoEnComun;
    }

    // *** Transforms ***

    @Override
    public Programa transform(Programa p) throws ExcepcionTransformer {
        setAlcanceActual(p.getAlcance());
        setTablaFunciones(p.getTablaFunciones());

        p = super.transform(p);

        setAlcanceActual(null);
        setTablaFunciones(null);
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
        setUltFunVisitada(df);
        setAlcanceActual(df.getAlcance());

        df = super.transform(df);

        setUltFunVisitada(null);
        setAlcanceActual(getAlcanceActual().getPadre());

        return df;
    }

    @Override
    public ParamDef transform(ParamDef pd) throws ExcepcionTransformer {
        pd = super.transform(pd);
        pd.setExpresion(convertirATipo(pd.getExpresion(), pd.getIdent().getTipo()));
        return pd;
    }

    @Override
    public Bloque transform(Bloque b) throws ExcepcionTransformer {
        setAlcanceActual(b.getAlcance());
        b = super.transform(b);
        setAlcanceActual(getAlcanceActual().getPadre());
        return b;
    }

    @Override
    public SimboloVariable transform(Identificador i) {
        // Reemplazar cada Identificador por el SimboloVariable correspondiente
        return getAlcanceActual().resolver(i.getNombre());
    }

    @Override
    public InvocacionFuncion transform(InvocacionFuncion invo) throws ExcepcionTransformer {
        invo = super.transform(invo);

        // No es necesario buscar en el alcance a las funciones predefinidas
        if (invo.getEsPredefinida()) {
            return invo;
        }

        DecFun df = getTablaFunciones().get(invo.getNombre());
        int cantArgs = invo.getArgs().size();

        // La invocación va a tener el mismo tipo que la función
        invo.setTipo(df.getTipo());

        // Validar el tipo de cada argumento, y convertir cuando sea necesario/posible
        for (int i = 0; i < cantArgs; i++) {
            // El argumento pasado en la invocación
            Expresion argInvo = invo.getArgs().get(i);

            // El tipo del parámetro, según se definió en la declaración
            Tipo tipoFormal = df.getParams().get(i).getTipo();

            // Guardar el argumento convertido en la invocación
            invo.getArgs().set(i, convertirATipo(argInvo, tipoFormal));
        }
        return invo;
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
            throw new ExcepcionTransformer(r,
                    "No se puede realizar esta comparación entre tipos boolean");
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

        // Y convertirla según el tipo de la función a la que pertenece
        r.setExpr(convertirATipo(r.getExpresion(), ultFunVisitada.getTipo()));

        // Además, guardar una referencia a la función en el return (la necesitamos para el IR)
        r.setFun(getUltFunVisitada());
        return r;
    }

    @Override
    public Mientras transform(Mientras m) throws ExcepcionTransformer {
        m = super.transform(m);
        if (m.getCondicion().getTipo() != Tipo.BOOLEAN) {
            throw new ExcepcionTransformer(m, "El tipo de la condición de «while» no es boolean");
        }
        return m;
    }

    @Override
    public Nodo transform(Cuando c) throws ExcepcionTransformer {
        super.transform(c);

        // Convierto el tipo de la expresión de los casos al tipo de la expresión principal.
        Tipo tipoCuando = c.getCondicion().getTipo();
        for (CasoCuando caso : c.getCasos()) {
            caso.setExpresion(convertirATipo(caso.getExpresion(), tipoCuando));
        }

        return c;
    }

    @Override
    public Sentencia transform(Para p) throws ExcepcionTransformer {
        p = (Para) super.transform(p);
        // No se puede convertir OperacionConversion a Identificador acá.
        // Para que soporte conversiones implícitas de flotante a entero
        // tendríamos que reemplazar en Para el Identificador por Expresion.
        if (p.getIdent().getTipo() != Tipo.INTEGER) {
            throw new ExcepcionTransformer(p, "El tipo de la variable a iterar no es integer");
        }
        return p;
    }
}
