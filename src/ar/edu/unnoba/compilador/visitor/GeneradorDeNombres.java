package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.aritmeticas.NegacionAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.conversiones.OperacionConversion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.logicas.NegacionLogica;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Param;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.util.Normalizador;

/** Asignar nombres normalizados a etiquetas, referencias y punteros
 *  a los distintos objetos que los requieran. */
public class GeneradorDeNombres extends Visitor {

    public void procesar(Programa p) throws ExcepcionVisitor {
        super.visit(p);
    }

    @Override
    public void visit(DecFun df) throws ExcepcionVisitor {
        super.visit(df);
        df.setEtiquetaFin(Normalizador.crearNomEtiqueta("ret"));
        df.setNombreFunIR(Normalizador.crearNomFun(df.getIdent().getNombre()));
        df.setPtroRet(Normalizador.crearNomPtroLcl("ret"));
        df.setRefIR(Normalizador.crearNomRef(df.getIdent().getNombre()));
    }

    @Override
    public void visit(Param p) throws ExcepcionVisitor {
        super.visit(p);
        SimboloVariable sv = (SimboloVariable) p.getIdent();
        sv.setRefIR(Normalizador.crearNomPtroLcl("ref"));
        sv.setPtroIR(Normalizador.crearNomPtroLcl("param"));
    }

    @Override
    public void visit(InvocacionFuncion invo) throws ExcepcionVisitor {
        super.visit(invo);
        // El refIR que va a contener el valor de la invocación a la función
        invo.setRefIR(Normalizador.crearNomRef("invo"));
    }

    @Override
    public void visit(Identificador ident) {
        // En esta etapa todos los los objetos Identificador son SimboloVariable
        SimboloVariable sv = (SimboloVariable) ident;
        sv.setPtroIR(sv.getEsGlobal() ?
                Normalizador.crearNomPtroGbl(ident.getNombre()) :
                Normalizador.crearNomPtroLcl(ident.getNombre()));
    }

    public void visit(OperacionBinaria ob) throws ExcepcionVisitor {
        super.visit(ob);
        ob.setRefIR(Normalizador.crearNomRef("ob"));
    }

    @Override
    public void visit(NegacionLogica neg) throws ExcepcionVisitor {
        super.visit(neg);
        neg.setRefIR(Normalizador.crearNomRef("not"));
    }

    @Override
    public void visit(NegacionAritmetica neg) throws ExcepcionVisitor {
        super.visit(neg);
        neg.setRefIR(Normalizador.crearNomRef("neg"));
    }

    @Override
    public void visit(OperacionConversion conv) throws ExcepcionVisitor {
        super.visit(conv);
        conv.setRefIR(Normalizador.crearNomRef("conv"));
    }

    @Override
    public void visit(Literal lit) {
        lit.setRefIR(Normalizador.crearNomRef("lit"));
    }

    @Override
    public void visit(Cadena c) {
        c.setPtroIR(Normalizador.crearNomPtroGbl("str"));
        c.setRefIR(Normalizador.crearNomPtroLcl("str"));
    }

}
