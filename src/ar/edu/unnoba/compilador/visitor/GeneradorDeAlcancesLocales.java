package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;

import java.util.Map;

/* Visitor para generar los alcances de los bloques, construir la tabla de
 * símbolos locales y verificar los alcances.
 */
public class GeneradorDeAlcancesLocales extends Visitor {
    private Alcance alcanceGlobal;
    private Alcance alcanceActual;
    private Map<String, SimboloFuncion> tablaFunciones;

    // Agregar la declaración al ámbito en el que se encuentra
    private void agregarSimbolo(Declaracion d) throws ExcepcionDeAlcance {
        if (alcanceGlobal == alcanceActual) {
            // Este Visitor sólo agrega símbolos locales, los globales ya los agregó el visitor
            // de alcance global
            return;
        }

        Identificador id = d.getIdent();
        String nombre = id.getNombre();

        // En este lenguaje no se pueden sobreescribir símbolos.
        // Dar error si ya existía un símbolo con este nombre en este ámbito o
        // en los ámbitos superiores.
        if (alcanceActual.containsKey(nombre)) {
            throw new ExcepcionDeAlcance(
                    String.format("La variable local «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            alcanceActual.get(nombre).getTipo()));
        }

        String nombreIR = Normalizador.getNvoNomVarLcl(nombre);
        Boolean esGlobal = false;

        SimboloVariable simbolo = new SimboloVariable(d, nombreIR, esGlobal);
        alcanceActual.put(nombre, simbolo);
    }

    private boolean estaEnElAlcance(Identificador i) {
        return (alcanceActual.resolver(i.getNombre()) != null);
    }

    private boolean estaEnElAlcance(InvocacionFuncion i) {
        return (tablaFunciones.get(i.getNombre()) != null);
    }


    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
        // Comenzar con alcance global para el encabezado.
        // Ya tiene el alcance establecido por el Visitor de alcance global.
        alcanceGlobal = alcanceActual = p.getAlcance();
        tablaFunciones = p.getTablaFunciones();
        super.visit(p);
        alcanceActual = null;
    }

    @Override
    public void visit(Bloque b) throws ExcepcionDeAlcance {
        // Establecer el alcance de cada bloque.
        // Crearlo como hijo del alcance actual
        alcanceActual = new Alcance(String.format("%d-%s", getID(), b.getNombre()), alcanceActual);
        b.setAlcance(alcanceActual);
        super.visit(b);

        // Ya se recorrió el subárbol, subir el nivel de alcance actual
        // (sino no se podrían definir símbolos con el mismo nombre en dos funciones distintas
        // por ejemplo)
        alcanceActual = alcanceActual.getPadre();
    }

    @Override
    public void visit(DecFun df) throws ExcepcionDeAlcance {
        // Generar un alcance nuevo para los parámetros
        alcanceActual = new Alcance(String.format("%d-%s", getID(), df.getNombre()), alcanceActual);
        df.setAlcance(alcanceActual);
        super.visit(df);
        alcanceActual = alcanceActual.getPadre();
    }

    public void visit(Param p) throws ExcepcionDeAlcance {
        agregarSimbolo(p);
        super.visit(p);
    }

    @Override
    public void visit(ParamDef pd) throws ExcepcionDeAlcance {
        agregarSimbolo(pd);
        super.visit(pd);
    }

    @Override
    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        agregarSimbolo(dv);
        super.visit(dv);
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionDeAlcance {
        agregarSimbolo(dvi);
        super.visit(dvi);
    }

    @Override
    public void visit(Identificador i) throws ExcepcionDeAlcance {
        if (!estaEnElAlcance(i)) {
            throw new ExcepcionDeAlcance(String.format("No se declaró la variable «%s»", i.getNombre()));
        }
        super.visit(i);
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionDeAlcance {
        // Si es predefinida ya está limitado por el parser, no es necesario realizar estas validaciones
        if (i.getEsPredefinida()) {
            super.visit(i);
            return;
        }

        // Validar definición contra la tabla de funciones
        if (!estaEnElAlcance(i)) {
            throw new ExcepcionDeAlcance(String.format("La función «%s» no está definida", i.getNombre()));
        }

        // Validar que la cantidad de argumentos pasados sea por lo menos la cantidad obligaria,
        // y no supere la cantidad total de parámetros, incluyendo los opcionales
        DecFun decFun = tablaFunciones.get(i.getNombre()).getDeclaracion();

        int cantArgsInvo = i.getArgs().size();
        int cantMinArgs = decFun.getCantArgsObligatorios();
        int cantMaxArgs = decFun.getParams().size();
        if (cantArgsInvo < cantMinArgs || cantArgsInvo > cantMaxArgs) {
            throw new ExcepcionDeAlcance(String.format(
                    "La función «%s» fue invocada con %d " + (cantArgsInvo == 1 ? "parámetro" : "parámetros") +
                            ", cuando requiere " + (cantMinArgs == cantMaxArgs ? "%d" : "entre %d y %d") + ".",
                    decFun.getNombre(), cantArgsInvo, cantMinArgs, cantMaxArgs));
        }

        super.visit(i);
    }
}
