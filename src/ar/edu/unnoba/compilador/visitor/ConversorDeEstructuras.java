package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.MenorIgual;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.Sentencia;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;

public class ConversorDeEstructuras extends Transformer {

    @Override
    public Bloque transform(Para p) throws ExcepcionDeTipos {
        p = (Para) super.transform(p);

        // bloqueNuevo va a contener el while
        Bloque bloqueNuevo = new Bloque("Conversión\nFOR a WHILE", false);

        Literal valDesde = new Literal(String.valueOf(p.getValorInicial()), Tipo.INTEGER, "Valor desde");
        Literal valHasta = new Literal(String.valueOf(p.getValorFinal()), Tipo.INTEGER, "Valor hasta");

        // Añadir al bloque la asignación con el valor inicial del contador
        Asignacion decDesde = new Asignacion(p.getIdent(), valDesde);
        bloqueNuevo.getSentencias().add(decDesde);

        // Crear condición del while según los valores del for
        Expresion condMientras = new MenorIgual(p.getIdent(), valHasta);

        // Añadir incremento del contador al final del bloque for
        Literal salto = new Literal(String.valueOf(p.getSalto()), Tipo.INTEGER, "Uno");
        Expresion inc = new Suma(p.getIdent(), salto);
        Asignacion asigInc = new Asignacion(p.getIdent(), inc);
        p.getBloqueSentencias().getSentencias().add(asigInc);

        // Crear while con la condición y el bloque for adaptado
        Mientras m = new Mientras(condMientras, p.getBloqueSentencias());

        bloqueNuevo.getSentencias().add(m);

        return bloqueNuevo;
    }

    @Override
    public Cuando transform(Cuando c) throws ExcepcionDeTipos {
        c = (Cuando) super.transform(c);
        return c;
    }

}
