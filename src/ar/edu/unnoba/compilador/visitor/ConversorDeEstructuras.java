package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeTipos;
import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.Suma;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.MenorIgual;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Literal;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarInicializada;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Para;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;

import java.util.ArrayList;

public class ConversorDeEstructuras extends Transformer {

    private int contVarTemp = 0;

    private int getContVarTemp() {
        contVarTemp = contVarTemp + 1;
        return contVarTemp;
    }

    @Override
    public Bloque transform(Para p) throws ExcepcionDeTipos {
        p = (Para) super.transform(p);

        // bloqueNuevo va a contener el while equivalente al for
        Bloque bloqueNuevo = new Bloque("Conversión\nFOR a WHILE", false);

        // Genero y defino el alcance padre para que no se rompa la cadena
        bloqueNuevo.setAlcance(new Alcance("Alcance conversión FOR -> WHEN"));
        bloqueNuevo.getAlcance().setPadre(p.getBloqueSentencias().getAlcance().getPadre());

        Literal valDesde = new Literal(String.valueOf(p.getValorInicial()), Tipo.INTEGER, "Valor desde");
        Literal valHasta = new Literal(String.valueOf(p.getValorFinal()), Tipo.INTEGER, "Valor hasta");

        // Añadir al bloque la asignación con el valor inicial del contador
        Asignacion decDesde = new Asignacion(p.getIdent(), valDesde);
        bloqueNuevo.getSentencias().add(decDesde);

        // Crear condición del while según los valores del for
        Expresion condMientras = new MenorIgual(p.getIdent(), valHasta);
        // Las relaciones son siempre boolean, y puedo setearlo directamente porque ya está validado
        condMientras.setTipo(Tipo.BOOLEAN);

        // Añadir incremento del contador al final del bloque for
        Literal salto = new Literal(String.valueOf(p.getSalto()), Tipo.INTEGER, "Salto");
        Expresion inc = new Suma(p.getIdent(), salto);
        Asignacion asigInc = new Asignacion(p.getIdent(), inc);
        p.getBloqueSentencias().getSentencias().add(asigInc);

        // Crear while con la condición y el bloque for adaptado
        Mientras m = new Mientras(condMientras, p.getBloqueSentencias());

        bloqueNuevo.getSentencias().add(m);

        return bloqueNuevo;
    }

    @Override
    public Bloque transform(Cuando c) throws ExcepcionDeTipos {
        c = (Cuando) super.transform(c);

        // El bloque que va a contener la estructura equivalente al when
        Bloque bloqueNuevo = new Bloque("Conversión\nCASE a IF", false);
        bloqueNuevo.setAlcance(new Alcance("Alcance conversión WHEN -> IF"));

        // La expresión del case pasa a estar en una nueva variable temporal
        String nombreVarTemp = "$tmp_" + getContVarTemp();
        Identificador identTemp = new Identificador(nombreVarTemp, c.getCondicion().getTipo());
        DecVarInicializada decVarTemp  = new DecVarInicializada(nombreVarTemp + " (WHEN -> IF)",
                identTemp, c.getCondicion());
        SimboloVariable simboloTemp = new SimboloVariable(decVarTemp);

        // Agrego la declaración en la lista de sentencias
        bloqueNuevo.getSentencias().add(decVarTemp);

        // Agrego el nuevo símbolo en el alcance
        bloqueNuevo.getAlcance().put(identTemp.getNombre(), simboloTemp);

        // Tengo que hacer esta chanchada para rescatar el alcance en el que está el when y no perder el padre
        Alcance alcancePadre = c.getCasos().get(0).getBloque().getAlcance().getPadre();
        bloqueNuevo.getAlcance().setPadre(alcancePadre);

        // Convertir los case a expresiones equivalentes en if
        SiEntoncesSino seGlobal = null;
        SiEntoncesSino seActual = null;

        for (CasoCuando cc : c.getCasos()) {
            // Crear la condición del if en base a la del when y el case
            Relacion cond = Relacion.getClaseRel(cc.getOp(), c.getCondicion(), cc.getExpr());
            cond.setTipo(Tipo.BOOLEAN);
            if (seActual == null) {
                // Primera vez que entro al for, creo el if principal
                seActual = new SiEntoncesSino(cond, cc.getBloque());
                seGlobal = seActual;
            } else {
                // Los subsiguientes ifs se encadenan al else del if anterior
                SiEntoncesSino seInterno = new SiEntoncesSino(cond, cc.getBloque());
                ArrayList<Nodo> ls = new ArrayList<>();
                ls.add(seInterno);
                Bloque bloqueElse = new Bloque("ELSE", ls, false);
                bloqueElse.setAlcance(cc.getBloque().getAlcance());
                seActual.setBloqueSino(bloqueElse);
                seActual = seInterno;
            }
        }

        // Agregar el else del switch (si lo tiene)
        if(seActual != null && c.getBloqueElse() != null) {
            seActual.setBloqueSino(c.getBloqueElse());
        }

        // Agrego al bloque el if externo
        bloqueNuevo.getSentencias().add(seGlobal);

        return bloqueNuevo;
    }

}
