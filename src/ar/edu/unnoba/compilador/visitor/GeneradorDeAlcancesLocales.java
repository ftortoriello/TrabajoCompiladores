package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;

import java.util.List;
import java.util.Map;

/* Visitor para generar los alcances de los bloques, construir la tabla de
 * símbolos locales y verificar los alcances.
 */
public class GeneradorDeAlcancesLocales extends Visitor<Void> {
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
        SimboloVariable simboloExistente = alcanceActual.resolver(nombre);
        if (simboloExistente != null) {
            throw new ExcepcionDeAlcance(
                    String.format("La variable local «%s» de tipo %s ya fue declarada previamente con tipo %s.",
                            nombre, id.getTipo(),
                            simboloExistente.getTipo()));
        }
        alcanceActual.put(nombre, new SimboloVariable(d));
    }

    private boolean estaEnElAlcance(Identificador i) {
        return (alcanceActual.resolver(i.getNombre()) != null);
    }

    private boolean estaEnElAlcance(InvocacionFuncion i) {
        return (tablaFunciones.get(i.getNombre()) != null);
    }


    @Override
    public Void visit(Programa p) throws ExcepcionDeAlcance {
        // Comenzar con alcance global para el encabezado.
        // Ya tiene el alcance establecido por el Visitor de alcance global.
        alcanceGlobal = alcanceActual = p.getAlcance();
        tablaFunciones = p.getTablaFunciones();
        super.visit(p);
        alcanceActual = null;
        return null;
    }

    @Override
    public Void visit(Bloque b) throws ExcepcionDeAlcance {
        // Establecer el alcance de cada bloque.
        // Crearlo como hijo del alcance actual
        alcanceActual = new Alcance(String.format("%d-%s", getID(), b.getNombre()), alcanceActual);
        b.setAlcance(alcanceActual);
        super.visit(b);

        // Ya se recorrió el subárbol, subir el nivel de alcance actual
        // (sino no se podrían definir símbolos con el mismo nombre en dos funciones distintas
        // por ejemplo)
        alcanceActual = alcanceActual.getPadre();
        return null;
    }

    @Override
    public Void visit(DecFuncion df) throws ExcepcionDeAlcance {
        // Generar un alcance nuevo para los parámetros
        alcanceActual = new Alcance(String.format("%d-%s", getID(), df.getNombre()), alcanceActual);
        df.setAlcance(alcanceActual);
        super.visit(df);
        alcanceActual = alcanceActual.getPadre();
        return null;
    }

    @Override
    public Void visit(DecVar dv) throws ExcepcionDeAlcance {
        agregarSimbolo(dv);
        return super.visit(dv);
    }

    @Override
    public Void visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        agregarSimbolo(dvi);
        return super.visit(dvi);
    }

    @Override
    public Void visit(Identificador i) throws ExcepcionDeAlcance {
        if (!estaEnElAlcance(i)) {
            throw new ExcepcionDeAlcance(String.format("No se declaró la variable «%s»", i.getNombre()));
        }
        return super.visit(i);
    }

    @Override
    public Void visit(InvocacionFuncion i) throws ExcepcionDeAlcance {
        if (!i.getEsPredefinida() && !estaEnElAlcance(i)) {
            throw new ExcepcionDeAlcance(String.format("No se definió la función «%s»", i.getNombre()));
        }
        return super.visit(i);
    }


    @Override
    protected Void procesarPrograma(Programa p, Void enc, Void blq) {
        return null;
    }

    @Override
    protected Void procesarNodo(Nodo n) {
        return null;
    }

    @Override
    protected Void procesarEncabezado(Encabezado e, List<Void> declaraciones) {
        return null;
    }

    @Override
    protected Void procesarBloque(Bloque b, List<Void> sentencias) {
        return null;
    }

    @Override
    protected Void procesarOperacionBinaria(OperacionBinaria ob, Void ei, Void ed) {
        return null;
    }

    @Override
    protected Void procesarVarInicializada(Void ident, Void expr) {
        return null;
    }

    @Override
    protected Void procesarAsignacion(Asignacion a, Void identificador, Void expresion) {
        return null;
    }

    @Override
    protected Void procesarDecFuncion(List<Void> args, Void cuerpo) {
        return null;
    }

    @Override
    protected Void procesarSiEntonces(Void cond, Void blq) {
        return null;
    }

    @Override
    protected Void procesarSiEntoncesSino(Void cond, Void blqSi, Void blqSino) {
        return null;
    }

    @Override
    protected Void procesarCuando(Cuando cc, Void expr, List<Void> casosCuando, Void blqElse) {
        return null;
    }

    @Override
    protected Void procesarCasoCuando(CasoCuando cc, Void expr, Void blq) {
        return null;
    }

    @Override
    protected Void procesarMientras(Mientras m, Void expr, Void blq) {
        return null;
    }

    @Override
    protected Void procesarRetorno(Retorno r, Void expr) {
        return null;
    }
}
