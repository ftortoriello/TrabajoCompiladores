package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFuncion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVar;
import ar.edu.unnoba.compilador.ast.operaciones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

public class ASTGraphviz extends Visitor<String>{

    private final Deque<Integer> parents;
    private int current_id = 0;

    public ASTGraphviz() {
        this.parents = new ArrayDeque<>();
    }

    // VISITAS
    // TODO: ver si se puede crear un método genérico visit, la mayoría son iguales
    // --------------------
    // Base
    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        /*
        resultado.append("layout=neato\n");
        resultado.append("node [\n" +
                "  shape = circle\n" +
                "  style=\"filled,bold\"\n" +
                "  color=black\n" +
                "  fillcolor=\"#F2F2F2\"\n" +
                "]");
         */

        // resultado.append("graph cluster_" + getID() + " {" +
        //                  "    label=\"Programa\";");
        resultado.append("graph Programa {");
        current_id = this.getID();
        resultado.append(this.procesarNodo(p));
        parents.push(current_id);
        resultado.append(super.visit(p));
        parents.pop();
        resultado.append("}");
        return resultado.toString();
    }
    @Override
    public String visit(Encabezado e) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        /*
        resultado.append("subgraph cluster_" + getID() + " {" +
                "   label=\"Encabezado\";");
        */
        current_id = this.getID();
        resultado.append(this.procesarNodo(e));
        parents.push(current_id);
        resultado.append(super.visit(e));
        parents.pop();
        //resultado.append("}");
        return resultado.toString();
    }
    @Override
    public String visit(Bloque b) throws ExcepcionDeAlcance {
        if (b.esCompuesto() || b.esProgramaPrincipal()) {
            StringBuilder resultado = new StringBuilder();
            current_id = this.getID();
            resultado.append(this.procesarNodo(b));
            parents.push(current_id);
            resultado.append(super.visit(b));
            parents.pop();
            return resultado.toString();
        } else {
            return super.visit(b);
        }
    }

    @Override
    public String visit(Constante c) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(c));
        return resultado.toString();
    }
    @Override
    public String visit(Identificador i) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(i));
        return resultado.toString();
    }
    @Override
    public String visit(InvocacionFuncion invo) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(invo));
        return resultado.toString();
    }
    // ----------

    // Operaciones
    @Override
    public String visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(ob));
        parents.push(current_id);
        resultado.append(super.visit(ob));
        parents.pop();
        return resultado.toString();
    }
    // ----------

    // Sentencias
    @Override
    public String visit(Asignacion a) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(a));
        parents.push(current_id);
        resultado.append(super.visit(a));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(DecFuncion df) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(df));
        parents.push(current_id);
        resultado.append(super.visit(df));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(DecVar dv) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(dv));
        parents.push(current_id);
        resultado.append(super.visit(dv));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(dvi));
        parents.push(current_id);
        resultado.append(super.visit(dvi));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(SiEntonces se) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(se));
        parents.push(current_id);
        resultado.append(super.visit(se));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(ses));
        parents.push(current_id);
        resultado.append(super.visit(ses));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(Cuando c) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(c));
        parents.push(current_id);
        resultado.append(super.visit(c));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(CasoCuando cc) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(cc));
        parents.push(current_id);
        resultado.append(super.visit(cc));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(Mientras m) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(m));
        parents.push(current_id);
        resultado.append(super.visit(m));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(Para p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(p));
        parents.push(current_id);
        resultado.append(super.visit(p));
        parents.pop();
        return resultado.toString();
    }
    @Override
    public String visit(Retorno r) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        current_id = this.getID();
        resultado.append(this.procesarNodo(r));
        parents.push(current_id);
        resultado.append(super.visit(r));
        parents.pop();
        return resultado.toString();
    }
    // ----------
    // --------------------

    // PROCESOS
    // --------------------
    // Base
    @Override
    protected String procesarNodo(Nodo n) {
        // Define el nodo n en el archivo .dot, y lo conecta a su padre si lo tiene
        Integer idPadre = parents.peekFirst();
        if(idPadre == null){
            return String.format("%1$s [label=\"%2$s\"]\n", current_id, n.getEtiqueta());
        }
        // TODO: mejorar el formato de los nodos.
        return String.format("%1$s [label=\"%2$s\"]\n%3$s -- %1$s\n", current_id, n.getEtiqueta(), idPadre);
    }
    @Override
    protected String procesarPrograma(Programa p, String enc, String blq) {
        return enc + blq;
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
    protected String procesarEncabezado(Encabezado e, List<String> sentencias) {
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach((sentencia) -> {
            resultado.append(sentencia);
        });
        return resultado.toString();
    }
    // ----------

    // Operaciones
    @Override
    protected String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        return ei+ed;
    }
    // ----------

    // Sentencias
    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) {
        return identificador+expresion;
    }
    @Override
    protected String procesarDecFuncion(List<String> args, String bloque) {
        StringBuilder strArgs = new StringBuilder();
        args.forEach(a -> strArgs.append(a));

        return strArgs + bloque;
    }
    @Override
    protected String procesarSiEntonces(String cond, String blq) {
        return cond + blq;
    }
    @Override
    protected String procesarSiEntoncesSino(String cond, String blqSi, String blqSino) {
        return cond + blqSi + blqSino;
    }
    @Override
    protected String procesarCuando(Cuando cc, String expr, List<String> casosCuando, String blqElse) {
        StringBuilder strCasos = new StringBuilder();
        casosCuando.forEach(c -> strCasos.append(c));

        return expr + strCasos + blqElse;
    }
    @Override
    protected String procesarCasoCuando(CasoCuando cc, String expr, String blq) {
        return expr + blq;
    }

    @Override
    protected String procesarMientras(Mientras m, String expr, String blq) {
        return expr + blq;
    }
    @Override
    protected String procesarVarInicializada(String ident, String expr) {
        return ident + expr;
    }
    @Override
    protected String procesarRetorno(Retorno r, String expr) {
        return expr;
    }
    // ----------
    // --------------------
}
