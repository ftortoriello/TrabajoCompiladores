package ar.edu.unnoba.compilador.visitor.transformer;

import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.base.Nodo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecVarIni;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.CasoCuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.Cuando;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;

import java.util.ArrayList;
import java.util.List;

public class ConversorDeEstructuras extends Transformer {
    @Override
    public Bloque transform(Cuando c) throws ExcepcionTransformer {
        c = (Cuando) super.transform(c);

        List<Nodo> sentencias = new ArrayList<>();

        // La expresión del case pasa a estar en una nueva variable temporal, para la cual tenemos
        // que crear su declaración.
        Identificador identAux = new Identificador("aux");
        DecVarIni decVarAux  = new DecVarIni(identAux, c.getCondicion());

        // Agregar la declaración en la lista de sentencias
        sentencias.add(decVarAux);

        List<CasoCuando> casos = c.getCasos();
        SiEntoncesSino seActual = null;

        // Recorrer desde el último case hasta el primero
        for (int i = casos.size() - 1; i >= 0; i--) {
            CasoCuando cc = casos.get(i);

            // Crear la condición del if en base a la del when y el case
            Relacion cond = Relacion.getClaseRel(cc.getOp(), identAux, cc.getExpresion());
            if (seActual == null) {
                // Si es el último case, crearlo con el else del when (si lo tiene)
                seActual = new SiEntoncesSino(cond, cc.getBloque(), c.getBloqueElse());
            } else {
                // Si no, crearlo con el else del case siguiente (que está en seActual)
                List<Nodo> ls = new ArrayList<>();
                ls.add(seActual);
                seActual = new SiEntoncesSino(cond, cc.getBloque(), new Bloque("ELSE", ls));
            }
        }

        // Agregar al bloque el if externo
        sentencias.add(seActual);

        // Retornar el bloque que va a contener la estructura equivalente al when
        return new Bloque("Conversión\\nWHEN a IF", sentencias);
    }
}
