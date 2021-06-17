package ar.edu.unnoba.compilador.visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.*;

public class ASTGraphviz extends Visitor {
    private final String etiqueta;
    private StringBuilder codigo;

    private final Deque<Integer> padres = new ArrayDeque<>();
    private Integer idNodoActual = 0;

    public ASTGraphviz(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String generarCodigo(Programa p) throws ExcepcionDeAlcance {
        super.procesar(p);
        return codigo.toString();
    }

    // Genera un ID nuevo, agrega al código dot un nodo, y lo conecta a su padre si lo tiene.
    private void armarStrNodo(String etiqueta, Integer tamanhoFuente, Integer color) {
        idNodoActual = getID();
        Integer idNodoPadre = padres.peekFirst();  // el que está en el tope ahora

        if (idNodoPadre == null) {
            // nodo raíz
            codigo.append(String.format(
                    "%1$s [label=\"%2$s\", fontsize=%3$d, fillcolor=%4$d, penwidth=5]\n",
                    idNodoActual, etiqueta, tamanhoFuente, color));
        } else {
            codigo.append(String.format(
                    "%1$s [label=\"%3$s\", fontsize=%4$d, fillcolor=%5$d]\n%2$s -- %1$s\n",
                    idNodoActual, idNodoPadre, etiqueta, tamanhoFuente, color));
        }
    }

    // Armarlo con estos valores predeterminados si no se pasan
    private void armarStrNodo(String etiqueta) {
        armarStrNodo(etiqueta, 18, 12);
    }
    private void armarStrEncabezado(String etiqueta) {
        armarStrNodo(etiqueta, 42, 11);
    }
    private void armarStrOperacion(String etiqueta) {
        armarStrNodo(etiqueta, 24, 7);
    }
    private void armarStrValor(String etiqueta) {
        armarStrNodo(etiqueta, 18, 9);
    }
    private void armarStrEstructura(String etiqueta) {
        armarStrNodo(etiqueta, 28, 8);
    }
    private void armarStrControl(String etiqueta) {
        armarStrNodo(etiqueta, 24, 4);
    }
    private void armarStrSimbolo(String etiqueta) {
        armarStrNodo(etiqueta, 18, 2);
    }
    private void armarStrDec(String etiqueta) {
        armarStrNodo(etiqueta, 28, 1);
    }
    private void armarStrOpLogica(String etiqueta) {
        armarStrNodo(etiqueta, 28, 5);
    }


    /* Base */

    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
        codigo = new StringBuilder();

        codigo.append("graph Programa {\n")
              .append("dpi = 72;\n")
              .append(String.format("label=\"%s\";\n", etiqueta))
              .append("bgcolor=aliceblue;\n")
              .append("fontsize=60;\n")
              .append("node [\n")
              .append("  style=\"filled,bold\";\n")
              .append("  color=black;\n")
              .append("  fillcolor=red;\n")
              .append("  colorscheme=set312\n")
              .append("]\n");

        armarStrNodo(p.getEtiqueta(), 48, 10);
        padres.push(idNodoActual);
        super.visit(p);
        padres.pop();

        codigo.append("}");
    }

    @Override
    public void visit(Encabezado e) throws ExcepcionDeAlcance {
        armarStrEncabezado(e.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(e);
        padres.pop();
    }

    @Override
    public void visit(Bloque b) throws ExcepcionDeAlcance {
        if (b.esProgramaPrincipal()) {
            armarStrEncabezado(b.getEtiqueta());
        } else {
            armarStrNodo(b.getEtiqueta(), 28, 3);
        }

        padres.push(idNodoActual);
        super.visit(b);
        padres.pop();
    }


    /* Sentencia de asignación */

    @Override
    public void visit(Asignacion a) throws ExcepcionDeAlcance {
        armarStrNodo(a.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(a);
        padres.pop();
    }


    /* Sentencias de declaración */

    @Override
    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        armarStrDec(dv.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(dv);
        padres.pop();
    }

    @Override
    public void visit(DecVarInicializada dvi) throws ExcepcionDeAlcance {
        armarStrDec(dvi.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(dvi);
        padres.pop();
    }

    @Override
    public void visit(DecFuncion df) throws ExcepcionDeAlcance {
        armarStrDec(df.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(df);
        padres.pop();
    }


    /* Sentencias de selección */

    @Override
    public void visit(SiEntonces se) throws ExcepcionDeAlcance {
        armarStrEstructura(se.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(se);
        padres.pop();
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        armarStrEstructura(ses.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(ses);
        padres.pop();
    }

    @Override
    public void visit(Cuando c) throws ExcepcionDeAlcance {
        armarStrNodo(c.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(c);
        padres.pop();
    }

    @Override
    public void visit(CasoCuando cc) throws ExcepcionDeAlcance {
        armarStrNodo(cc.getEtiqueta(), 26, 6);
        padres.push(idNodoActual);
        super.visit(cc);
        padres.pop();
    }


    /* Sentencias de iteración */

    @Override
    public void visit(Mientras m) throws ExcepcionDeAlcance {
        armarStrEstructura(m.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(m);
        padres.pop();
    }

    @Override
    public void visit(Para p) throws ExcepcionDeAlcance {
        armarStrEstructura(p.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(p);
        padres.pop();
    }


    /* Sentencias de control */

    @Override
    public void visit(Retorno r) throws ExcepcionDeAlcance {
        armarStrControl(r.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(r);
        padres.pop();
    }

    @Override
    public void visit(Continuar c) throws ExcepcionDeAlcance {
        armarStrControl(c.getEtiqueta());
    }

    @Override
    public void visit(Salir s) throws ExcepcionDeAlcance {
        armarStrControl(s.getEtiqueta());
    }


    /* Operaciones */

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        if (ob instanceof Relacion) {
            // TODO: Buscar otro color no usado?
            armarStrNodo(ob.getEtiqueta(), 26, 6);
        } else if (ob instanceof OperacionBinariaLogica) {
            armarStrOpLogica(ob.getEtiqueta());
        } else {
            armarStrOperacion(ob.getEtiqueta());
        }

        padres.push(idNodoActual);
        super.visit(ob);
        padres.pop();
    }

    @Override
    public void visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        if (ou instanceof NegacionLogica) {
            armarStrOpLogica(ou.getEtiqueta());
        } else {
            armarStrOperacion(ou.getEtiqueta());
        }
        padres.push(idNodoActual);
        super.visit(ou);
        padres.pop();
    }


    /* Valores */

    @Override
    public void visit(Literal l) {
        armarStrValor(l.getEtiqueta());
    }

    @Override
    public void visit(Identificador i) {
        if (i instanceof SimboloVariable) {
            armarStrSimbolo(i.getEtiqueta());
        } else {
            armarStrValor(i.getEtiqueta());
        }
    }

    @Override
    public void visit(InvocacionFuncion i) {
        if (i instanceof SimboloFuncion) {
            armarStrSimbolo(i.getEtiqueta());
        } else {
            armarStrValor(i.getEtiqueta());
        }
    }
}
