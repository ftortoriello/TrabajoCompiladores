package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.DecVar;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;

public class ASTGraphviz extends Visitor<String>{

    private final Deque<Integer> parents;
    private int current_id = 0;

    public ASTGraphviz() {
        this.parents = new ArrayDeque<>();
    }

    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        // TODO: dejar como estaba, lo modifiqué para que no pase del nodo raíz
        StringBuilder resultado = new StringBuilder();
        resultado.append("graph G {");
        current_id = this.getID();
        resultado.append(this.procesarNodo(p));
        parents.push(current_id);
        // resultado.append(super.visit(p));
        parents.pop();
        resultado.append("}");
        return resultado.toString();
    }

    @Override
    protected String procesarNodo(Nodo n) {
        Integer idPadre = parents.peekFirst();
        if(idPadre == null){
            return String.format("%1$s [label=\"%2$s\"]\n", current_id, n.getEtiqueta());
        }
        return String.format("%1$s [label=\"%2$s\"]\n%3$s -- %1$s\n", current_id, n.getEtiqueta(), idPadre);
    }

    @Override
    public String visit(Asignacion a) throws ExcepcionDeAlcance{
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(a));
        parents.push(current_id);
        resultado.append(super.visit(a));
        parents.pop();
        return resultado.toString();
    }

    @Override
    public String visit(OperacionBinaria ob) throws ExcepcionDeAlcance{
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(ob));
        parents.push(current_id);
        resultado.append(super.visit(ob));
        parents.pop();
        return resultado.toString();
    }

    @Override
    public String visit(DecVar dv) throws ExcepcionDeAlcance{
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(dv));
        parents.push(current_id);
        resultado.append(super.visit(dv));
        parents.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Bloque b) throws ExcepcionDeAlcance{
        if(b.esProgramaPrincipal()){
            return super.visit(b);
        }else{
            StringBuilder resultado = new StringBuilder();
            current_id = this.getID();
            resultado.append(this.procesarNodo(b));
            parents.push(current_id);
            resultado.append(super.visit(b));
            parents.pop();
            return resultado.toString();
        }
    }

    @Override
    public String visit(Constante c) {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(c));
        parents.push(current_id);
        parents.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Identificador i) {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(i));
        parents.push(current_id);
        parents.pop();
        return resultado.toString();
    }

    @Override
    public String visit(InvocacionFuncion invo) {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(invo));
        parents.push(current_id);
        parents.pop();
        return resultado.toString();
    }


    @Override
    protected String procesarBloque(Bloque b, List<String> sentencias) {
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach((sentencia) -> {
            resultado.append(sentencia);
        });
        return resultado.toString();
    }

    @Override
    protected String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        return ei+ed;
    }

    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) {
        return identificador+expresion;
    }

}
