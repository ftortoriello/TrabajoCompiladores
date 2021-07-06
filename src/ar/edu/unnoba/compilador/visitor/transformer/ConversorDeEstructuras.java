package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.expresiones.valor.SimboloVariable;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

import java.util.ArrayList;
import java.util.List;

public class ConversorDeEstructuras extends Transformer {

    // Este método quedó más complicados de lo que debería porque para los nodos nuevos que tienen
    // que crearse en las transformaciones hacemos muchas cosas* a mano, para evitar tener que pasar
    // los generadores de alcance una segunda vez...

    // *: generar alcances, generar simboloVariable, agregar los Simbolos en los Alcances,
    // setear el padre de los alcances, setear los tipos y creo que nada más.

    @Override
    public Bloque transform(Cuando c) throws ExcepcionTransformer {
        c = (Cuando) super.transform(c);

        // El bloque que va a contener la estructura equivalente al when
        Bloque bloqueNuevo = new Bloque("Conversión\\nCASE a IF", false);
        bloqueNuevo.setAlcance(new Alcance("Alcance conversión WHEN -> IF"));

        // La expresión del case pasa a estar en una nueva variable temporal, para la
        // cual tengo que crear su símbolo y añadirlo al alcance.
        Identificador identAux = new Identificador("aux", c.getCondicion().getTipo());
        DecVarIni decVarAux  = new DecVarIni(identAux, c.getCondicion());
        SimboloVariable simboloAux = new SimboloVariable(decVarAux, false);
        decVarAux.setIdent(simboloAux);

        // Agrego la declaración en la lista de sentencias
        bloqueNuevo.getSentencias().add(decVarAux);

        // Agrego el nuevo símbolo en el alcance
        bloqueNuevo.getAlcance().put(identAux.getNombre(), simboloAux);

        // Tengo que hacer esta chanchada para rescatar el alcance en el que está el when y no perder el padre
        Alcance alcancePadre = c.getCasos().get(0).getBloque().getAlcance().getPadre();
        bloqueNuevo.getAlcance().setPadre(alcancePadre);

        // Convertir los case a expresiones equivalentes en if
        SiEntoncesSino seGlobal = null;
        SiEntoncesSino seActual = null;

        for (CasoCuando cc : c.getCasos()) {
            // Crear la condición del if en base a la del when y el case
            Relacion cond = Relacion.getClaseRel(cc.getOp(), simboloAux, cc.getExpresion());
            cond.setTipo(Tipo.BOOLEAN);
            if (seActual == null) {
                // Primera vez que entro al for, creo el if principal
                cc.getBloque().getAlcance().setPadre(bloqueNuevo.getAlcance());
                seActual = new SiEntoncesSino(cond, cc.getBloque());
                seGlobal = seActual;
            } else {
                // Los subsiguientes ifs se encadenan al else del if anterior
                SiEntoncesSino seInterno = new SiEntoncesSino(cond, cc.getBloque());
                List<Nodo> ls = new ArrayList<>();
                ls.add(seInterno);
                Bloque bloqueElse = new Bloque("ELSE", ls, false);
                bloqueElse.setAlcance(cc.getBloque().getAlcance());
                bloqueElse.getAlcance().setPadre(bloqueNuevo.getAlcance());
                seActual.setBloqueSino(bloqueElse);
                seActual = seInterno;
            }
        }

        // Agregar el else del switch (si lo tiene)
        if(seActual != null && c.getBloqueElse() != null) {
            seActual.setBloqueSino(c.getBloqueElse());
            seActual.getBloqueSino().getAlcance().setPadre(bloqueNuevo.getAlcance());
        }

        // Agrego al bloque el if externo
        bloqueNuevo.getSentencias().add(seGlobal);

        return bloqueNuevo;
    }

}
