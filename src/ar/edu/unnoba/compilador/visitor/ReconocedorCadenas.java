package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.expresiones.Cadena;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.util.Normalizador;

import java.util.ArrayList;
import java.util.List;


/**
 * Visitor que recolecta todas las cadenas utilizadas en el programa, para guardarlas y luego poder
 * declarar las variables globales en el IR. Se ejecuta inmediatamnente antes de generar el código.
 */
public class ReconocedorCadenas extends Visitor {

    private final List<Cadena> arrCadenas = new ArrayList<>();

    public void procesar(Programa p) throws ExcepcionVisitor {
        super.visit(p);
        p.setArrCadenas(arrCadenas);
    }

    @Override
    public void visit(Cadena c) {
        c.setPtroIR(Normalizador.crearNomPtroGbl("str"));
        c.setRefIR(Normalizador.crearNomPtroLcl("str"));
        arrCadenas.add(c);
    }

}
