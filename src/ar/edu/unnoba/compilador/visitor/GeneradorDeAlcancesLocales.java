package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Visitor para generar los alcances de los bloques, construir la tabla de símbolos locales y
 * verificar los alcances.
 */
public class GeneradorDeAlcancesLocales extends Visitor {
    private Alcance alcanceGlobal;
    private Alcance alcanceActual;
    private Map<String, DecFun> tablaFunciones;
    private Set<String> funPredefUsadas;

    /** Agregar la declaración al ámbito en el que se encuentra. */
    private void agregarSimbolo(Declaracion d) throws ExcepcionVisitor {
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
        if (estaEnElAlcance(id)) {
            throw new ExcepcionVisitor(d,
                    String.format("La variable local de tipo %s ya fue declarada previamente con tipo %s.",
                            id.getTipo(), alcanceActual.resolver(nombre).getTipo()));
        }

        SimboloVariable simbolo = new SimboloVariable(d, false);
        alcanceActual.put(nombre, simbolo);
    }

    private boolean estaEnElAlcance(Identificador i) {
        return (alcanceActual.resolver(i.getNombre()) != null);
    }

    private boolean estaEnElAlcance(InvocacionFuncion i) {
        return (tablaFunciones.get(i.getNombre()) != null);
    }


    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
        // Comenzar con alcance global para el encabezado.
        // Ya tiene el alcance establecido por el Visitor de alcance global.
        alcanceGlobal = alcanceActual = p.getAlcance();
        tablaFunciones = p.getTablaFunciones();
        funPredefUsadas = new HashSet<>();
        p.setFunPredefUsadas(funPredefUsadas);

        super.visit(p);

        alcanceActual = null;
    }

    @Override
    public void visit(Bloque b) throws ExcepcionVisitor {
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
    public void visit(DecFun df) throws ExcepcionVisitor {
        // Generar un alcance nuevo para los parámetros
        alcanceActual = new Alcance(String.format("%d-%s", getID(), df.getNombre()), alcanceActual);
        df.setAlcance(alcanceActual);
        super.visit(df);
        alcanceActual = alcanceActual.getPadre();
    }

    @Override
    public void visit(Param p) throws ExcepcionVisitor {
        agregarSimbolo(p);
    }

    @Override
    public void visit(ParamDef pd) throws ExcepcionVisitor {
        agregarSimbolo(pd);
    }

    @Override
    public void visit(DecVar dv) throws ExcepcionVisitor {
        agregarSimbolo(dv);
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        agregarSimbolo(dvi);
        super.visit(dvi);
    }

    @Override
    public void visit(Identificador i) throws ExcepcionVisitor {
        if (!estaEnElAlcance(i)) {
            throw new ExcepcionVisitor(i, String.format(
                    "No se declaró la variable «%s»", i.getNombre()));
        }
        super.visit(i);
    }

    /**
     * Validar que la cantidad de argumentos pasados sea por lo menos la cantidad obligaria,
     * y no supere la cantidad total de parámetros, incluyendo los opcionales.
     */
    private void validarParametros(DecFun decFun, InvocacionFuncion i) throws ExcepcionVisitor {
        int cantArgsInvo = i.getArgs().size();
        int cantMinArgs = decFun.getCantArgsObligatorios();
        int cantMaxArgs = decFun.getParams().size();
        if (cantArgsInvo < cantMinArgs || cantArgsInvo > cantMaxArgs) {
            throw new ExcepcionVisitor(decFun, String.format(
                    "La función fue invocada con %d parámetro" + (cantArgsInvo == 1 ? "" : "s") +
                            ", pero requiere " +
                            (cantMinArgs == cantMaxArgs ? "%d" : "entre %d y %d") + ".",
                    cantArgsInvo, cantMinArgs, cantMaxArgs));
        }
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionVisitor {
        // Validar definición contra la tabla de funciones
        if (!estaEnElAlcance(i)) {
            throw new ExcepcionVisitor(i, "La función no está definida");
        }

        DecFun decFun = tablaFunciones.get(i.getNombre());
        if (i.esPredefinida()) {
            funPredefUsadas.add(i.getNombre());
        } else {
            // Si es predefinida ya están limitados los parámetros por el parser, no es necesario
            // validarlos acá
            validarParametros(decFun, i);
        }

        // Para poder eliminar las declaraciones no usadas
        decFun.setInvocada();

        super.visit(i);
    }
}
