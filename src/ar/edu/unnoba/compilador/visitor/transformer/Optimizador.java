package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Encabezado;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.literal.Literal;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.Declaracion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.ParamDef;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

import java.util.ArrayList;
import java.util.List;

/* Transformer que aplica Constant Folding. */

public class Optimizador extends Transformer {
    @Override
    public Expresion transform(OperacionBinaria ob) throws ExcepcionTransformer {
        return super.transform(ob).evaluar();
    }

    @Override
    public Expresion transform(Relacion r) throws ExcepcionTransformer {
        return super.transform(r).evaluar();
    }

    @Override
    public Expresion transform(OperacionUnaria ou) throws ExcepcionTransformer {
        return super.transform(ou).evaluar();
    }

    @Override
    public Encabezado transform(Encabezado e) throws ExcepcionTransformer {
        List<Declaracion> declaraciones = new ArrayList<>();
        for (Declaracion d : e.getDeclaraciones()) {
            boolean agregar = true;
            if (d instanceof DecFun) {
                agregar = ((DecFun) d).esInvocada();
            }
            if (agregar) declaraciones.add((Declaracion) d.accept(this));
        }
        e.setDeclaraciones(declaraciones);
        return e;
    }

    @Override
    public ParamDef transform(ParamDef pi) throws ExcepcionTransformer {
        ParamDef paramOptimizado = super.transform(pi);
        if (!(paramOptimizado.getExpresion() instanceof Literal)) {
            throw new ExcepcionTransformer(pi, "La expresi??n del valor del par??metro " +
                    "por defecto debe estar formada s??lo por literales");
        } else {
            return paramOptimizado;
        }
    }
}
