package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Encabezado;
import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas.NegacionAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.OperacionConversion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.InvocacionFuncion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;

import java.util.ArrayDeque;
import java.util.Deque;

public class ASTGraphviz extends Visitor {
    private final String etiqueta;
    private StringBuilder codigo;

    private final Deque<Integer> padres = new ArrayDeque<>();
    private Integer idNodoActual = 0;

    public ASTGraphviz(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Función principal. */
    public String generarCodigo(Programa p) throws ExcepcionVisitor {
        super.procesar(p);
        return codigo.toString();
    }

    /** Generar un ID nuevo, agregar al código dot un nodo, y conectarlo a su padre si lo tiene. */
    private void armarStrNodo(String etiqueta, Integer tamanhoFuente, Integer color) {
        idNodoActual = getID();
        Integer idNodoPadre = padres.peek();  // el que está en el tope ahora

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
    private void armarStrRelacion(String etiqueta) {
        armarStrNodo(etiqueta, 26, 6);
    }


    // *** Base ***

    @Override
    public void visit(Programa p) throws ExcepcionVisitor {
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
    public void visit(Encabezado e) throws ExcepcionVisitor {
        armarStrEncabezado(e.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(e);
        padres.pop();
    }

    @Override
    public void visit(Bloque b) throws ExcepcionVisitor {
        if (b.esProgramaPrincipal()) {
            armarStrEncabezado(b.getEtiqueta());
        } else {
            armarStrNodo(b.getEtiqueta(), 28, 3);
        }

        padres.push(idNodoActual);
        super.visit(b);
        padres.pop();
    }


    // *** Sentencia de asignación ***

    @Override
    public void visit(Asignacion a) throws ExcepcionVisitor {
        armarStrNodo(a.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(a);
        padres.pop();
    }


    // *** Sentencias de declaración ***

    @Override
    public void visit(DecVar dv) throws ExcepcionVisitor {
        armarStrDec(dv.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(dv);
        padres.pop();
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionVisitor {
        armarStrDec(dvi.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(dvi);
        padres.pop();
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        armarStrDec(df.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(df);
        padres.pop();
    }

    @Override
    public void visit(Param p) throws ExcepcionVisitor {
        armarStrDec(p.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(p);
        padres.pop();
    }

    @Override
    public void visit(ParamDef pd) throws ExcepcionVisitor {
        armarStrDec(pd.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(pd);
        padres.pop();
    }


    // *** Sentencias de selección ***

    @Override
    public void visit(SiEntonces se) throws ExcepcionVisitor {
        armarStrEstructura(se.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(se);
        padres.pop();
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionVisitor {
        armarStrEstructura(ses.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(ses);
        padres.pop();
    }

    @Override
    public void visit(Cuando c) throws ExcepcionVisitor {
        armarStrNodo(c.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(c);
        padres.pop();
    }

    @Override
    public void visit(CasoCuando cc) throws ExcepcionVisitor {
        armarStrRelacion(cc.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(cc);
        padres.pop();
    }


    // *** Sentencias de iteración ***

    @Override
    public void visit(Mientras m) throws ExcepcionVisitor {
        armarStrEstructura(m.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(m);
        padres.pop();
    }


    // *** Sentencias de control ***

    @Override
    public void visit(Retorno r) throws ExcepcionVisitor {
        armarStrControl(r.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(r);
        padres.pop();
    }

    @Override
    public void visit(Continuar c) {
        armarStrControl(c.getEtiqueta());
    }

    @Override
    public void visit(Salir s) {
        armarStrControl(s.getEtiqueta());
    }


    // *** Operaciones ***

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionVisitor {
        if (ob instanceof Relacion) {
            armarStrRelacion(ob.getEtiqueta());
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
    public void visit(NegacionAritmetica neg) throws ExcepcionVisitor {
        armarStrOperacion(neg.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(neg);
        padres.pop();
    }

    @Override
    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        armarStrOpLogica(neg.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(neg);
        padres.pop();
    }

    @Override
    public void visit(OperacionConversion conv) throws ExcepcionVisitor {
        armarStrOperacion(conv.getEtiqueta());
        padres.push(idNodoActual);
        super.visit(conv);
        padres.pop();
    }


    // *** Valores ***

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
        armarStrValor(i.getEtiqueta());
    }
}
