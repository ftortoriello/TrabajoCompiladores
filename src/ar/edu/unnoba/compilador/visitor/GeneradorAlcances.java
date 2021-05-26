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

public class GeneradorAlcances extends Visitor<Void> {

    private Alcance alcance_actual;
    private Alcance alcance_global;


    public void procesar(Programa programa) throws ExcepcionDeAlcance{
        programa.getCuerpo().setAlcance(new Alcance("global"));
        alcance_global = alcance_actual = programa.getCuerpo().getAlcance();
        this.visit(programa);
    }

    private Object agregarSimbolo(String nombre, Object s){
        return this.alcance_actual.putIfAbsent(nombre, s);
    }

    @Override
    public Void visit(DecVar dv) throws ExcepcionDeAlcance {
        Variable var = new Variable(dv);
        Object result = this.agregarSimbolo(var.getDeclaracion().getIdent().getNombre(), dv);
        if(result!=null){
            throw new ExcepcionDeAlcance(
                    String.format("El nombre de la variable %1$s de tipo %2$s fue utilizado previamente\"]\n",
                            dv.getIdent().getNombre(), dv.getTipo() ));
        }
        return null;
    }

    @Override
    protected Void procesarEncabezado(Encabezado encabezado, List<Void> sentencias) {
        return null;
    }

    @Override
    protected Void procesarBloque(Bloque bloque, List<Void> sentencias) {
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
