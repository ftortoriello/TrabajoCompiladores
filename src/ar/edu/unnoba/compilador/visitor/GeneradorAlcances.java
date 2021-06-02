package ar.edu.unnoba.compilador.visitor;

import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Variable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;

// FIXME: En algún momento rompí todo
public class GeneradorAlcances extends Visitor<Void> {
    private Alcance alcanceActual;
    private Alcance alcanceGlobal;

    public void procesar(Programa programa) throws ExcepcionDeAlcance {
        alcanceGlobal = alcanceActual = new Alcance("global");
        programa.getCuerpo().setAlcance(alcanceGlobal);
        this.visit(programa);
    }

    private Nodo agregarSimbolo(String nombre, Nodo s) {
        return this.alcanceActual.putIfAbsent(nombre, s);
    }


    // TODO: agregar visit para Programa, Bloque y Asignación

    @Override
    public Void visit(DecVar dv) throws ExcepcionDeAlcance {
        // Agrega la declaración al ámbito en el que se encuentra
        Variable var = new Variable(dv);
        Nodo result = this.agregarSimbolo(var.getDeclaracion().getIdent().getNombre(), dv);
        if (result != null) {
            throw new ExcepcionDeAlcance(
                    String.format("El nombre de la variable %1$s de tipo %2$s fue utilizado previamente.",
                            dv.getIdent().getNombre(), dv.getTipo()));
        }
        return null;
    }

    @Override
    protected Void procesarEncabezado(Encabezado e, List<Void> sentencias) {
        return null;
    }

    @Override
    protected Void procesarBloque(Bloque b, List<Void> sentencias) {
        // FIXME: poner el nombre de la función?
        alcanceActual = new Alcance(String.format("%s-%d", b.getNombre(), this.getID()));
        b.setAlcance(alcanceActual);
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
    protected Void procesarNodo(Nodo n) {
        return null;
    }

    @Override
    protected Void procesarPrograma(Programa p, Void enc, Void blq) {
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
