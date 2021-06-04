package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Conjuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.Disyuncion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Valor;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Control;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.*;

public class ASTGraphviz extends Visitor<String> {
    private final Deque<Integer> padres;
    private int idNodoActual = 0;

    public ASTGraphviz() {
        this.padres = new ArrayDeque<>();
    }

    private String armarStrNodo(Integer tamanhoFuente, Integer color, String etiqueta, Integer idPadre) {
        // Función auxiliar para armar los nodos en dot
        return String.format("%1$s [label=\"%2$s\", fontsize=" + tamanhoFuente + ", fillcolor=" + color + "]\n" +
                "%3$s -- %1$s\n", idNodoActual, etiqueta, idPadre);
    }

    // *** VISITAS ***
    // TODO: ver si se puede crear un método genérico visit, la mayoría son iguales

    // Base
    @Override
    public String visit(Programa p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();

        resultado.append("graph Programa {" +
                "label=\"Árbol de sintaxis abstracta (Conti - Tortoriello)\";\n" +
                "bgcolor=aliceblue;\n" +
                "fontsize=60;\n");
        resultado.append("node [\n" +
                "  style=\"filled,bold\";\n" +
                "  color=black;\n" +
                "  fillcolor=red;\n" +
                "  colorscheme=set312\n" +
                "]\n");
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(p));
        padres.push(idNodoActual);
        resultado.append(super.visit(p));
        padres.pop();
        resultado.append("}");
        return resultado.toString();
    }

    @Override
    public String visit(Encabezado e) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = getID();
        resultado.append(this.procesarNodo(e));
        padres.push(idNodoActual);
        resultado.append(super.visit(e));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Bloque b) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(b));
        padres.push(idNodoActual);
        resultado.append(super.visit(b));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Literal c) {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(c));
        return resultado.toString();
    }

    @Override
    public String visit(Identificador i) {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(i));
        return resultado.toString();
    }

    @Override
    public String visit(InvocacionFuncion invo) {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(invo));
        return resultado.toString();
    }


    // Operaciones

    @Override
    public String visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(ob));
        padres.push(idNodoActual);
        resultado.append(super.visit(ob));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(ou));
        padres.push(idNodoActual);
        resultado.append(super.visit(ou));
        padres.pop();
        return resultado.toString();
    }


    // Sentencias
    @Override
    public String visit(Asignacion a) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(a));
        padres.push(idNodoActual);
        resultado.append(super.visit(a));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(DecFuncion df) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(df));
        padres.push(idNodoActual);
        resultado.append(super.visit(df));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(DecVar dv) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(dv));
        padres.push(idNodoActual);
        resultado.append(super.visit(dv));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(dvi));
        padres.push(idNodoActual);
        resultado.append(super.visit(dvi));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(SiEntonces se) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(se));
        padres.push(idNodoActual);
        resultado.append(super.visit(se));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(ses));
        padres.push(idNodoActual);
        resultado.append(super.visit(ses));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Cuando c) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(c));
        padres.push(idNodoActual);
        resultado.append(super.visit(c));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(CasoCuando cc) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(cc));
        padres.push(idNodoActual);
        resultado.append(super.visit(cc));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Mientras m) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(m));
        padres.push(idNodoActual);
        resultado.append(super.visit(m));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Para p) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(p));
        padres.push(idNodoActual);
        resultado.append(super.visit(p));
        padres.pop();
        return resultado.toString();
    }

    @Override
    public String visit(Retorno r) throws ExcepcionDeAlcance {
        StringBuilder resultado = new StringBuilder();
        idNodoActual = this.getID();
        resultado.append(this.procesarNodo(r));
        padres.push(idNodoActual);
        resultado.append(super.visit(r));
        padres.pop();
        return resultado.toString();
    }


    // *** PROCESOS ***

    // Base

    @Override
    protected String procesarNodo(Nodo n) {
        // Define el nodo n en el archivo .dot, y lo conecta a su padre si lo tiene

        Integer idPadre = padres.peekFirst();

        if (idPadre == null) {
            return String.format("%1$s [label=\"%2$s\", fontsize=48, fillcolor=10, penwidth=5]\n",
                    idNodoActual, n.getEtiqueta());
        } else if (n instanceof Encabezado) {
            return armarStrNodo(42, 11, n.getEtiqueta(), idPadre);
        } else if (n instanceof Bloque) {
            if (((Bloque) n).esProgramaPrincipal()) {
                return armarStrNodo(42, 11, n.getEtiqueta(), idPadre);
            } else {
                return armarStrNodo(28, 3, n.getEtiqueta(), idPadre);
            }
        } else if (n instanceof Declaracion) {
            return armarStrNodo(28, 1, n.getEtiqueta(), idPadre);
        } else if (n instanceof Disyuncion || n instanceof Conjuncion || n instanceof NegacionLogica) {
            return armarStrNodo(28, 5, n.getEtiqueta(), idPadre);
        } else if (n instanceof Relacion || n instanceof CasoCuando) {
            return armarStrNodo(26, 6, n.getEtiqueta(), idPadre);
        } else if (n instanceof OperacionBinaria || n instanceof OperacionUnaria) {
            return armarStrNodo(24, 7, n.getEtiqueta(), idPadre);
        } else if (n instanceof Seleccion || n instanceof Para || n instanceof Mientras) {
            return armarStrNodo(28, 8, n.getEtiqueta(), idPadre);
        } else if (n instanceof Valor) {
            return armarStrNodo(18, 9, n.getEtiqueta(), idPadre);
        } else if (n instanceof Control) {
            return armarStrNodo(24, 4, n.getEtiqueta(), idPadre);
        } else {
            return armarStrNodo(18, 12, n.getEtiqueta(), idPadre);
        }
    }

    @Override
    protected String procesarPrograma(Programa p, String enc, String blq) {
        return enc + blq;
    }

    @Override
    protected String procesarEncabezado(Encabezado e, List<String> sentencias) {
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach(resultado::append);
        return resultado.toString();
    }

    @Override
    protected String procesarBloque(Bloque b, List<String> sentencias) {
        StringBuilder resultado = new StringBuilder();
        sentencias.forEach(resultado::append);
        return resultado.toString();
    }


    // Operaciones

    @Override
    protected String procesarOperacionBinaria(OperacionBinaria ob, String ei, String ed) {
        return ei + ed;
    }


    // Sentencias

    @Override
    protected String procesarAsignacion(Asignacion a, String identificador, String expresion) {
        return identificador + expresion;
    }

    @Override
    protected String procesarDecFuncion(List<String> args, String bloque) {
        StringBuilder strArgs = new StringBuilder();
        args.forEach(strArgs::append);

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
        casosCuando.forEach(strCasos::append);

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
}
