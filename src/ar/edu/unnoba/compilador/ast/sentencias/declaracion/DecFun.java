package ar.edu.unnoba.compilador.ast.sentencias.declaracion;

import ar.edu.unnoba.compilador.ast.base.Alcance;
import ar.edu.unnoba.compilador.ast.base.Bloque;
import ar.edu.unnoba.compilador.ast.expresiones.valor.Identificador;
import ar.edu.unnoba.compilador.excepciones.ExcepcionTransformer;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Declaración de una función. */
public class DecFun extends Declaracion {
    private List<Param> params;
    private Bloque bloque;
    private Alcance alcance;

    /** Si la función se invoca en algún momento. */
    private boolean usada = false;

    /** El nombre de la función, normalizado para usarse en IR. */
    private String nombreFunIR;
    /** El puntero en el que se va a guardar el valor de retorno. */
    private String ptroRet;
    /** Referencia utilizada en conjunto con el ptro. para cargar el valor. */
    private String refIR;
    /** Después de esta etiqueta encuentro el return de la función. */
    private String etiquetaFin;

    public DecFun(Identificador ident, List<Param> params, Bloque bloque) {
        super(ident);
        // Invierto el orden de los parámetros porque debido a la forma de las reglas los lee al revés
        Collections.reverse(params);
        bloque.setNombre("Cuerpo\\nFUNCIÓN");
        this.params = params;
        this.bloque = bloque;
    }

    public DecFun(Identificador ident, Bloque bloque) {
        super(ident);
        bloque.setNombre("Cuerpo\\nFUNCIÓN");
        this.params = new ArrayList<>();
        this.bloque = bloque;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public Bloque getBloque() {
        return bloque;
    }

    public void setBloque(Bloque bloque) {
        this.bloque = bloque;
    }

    public Alcance getAlcance() {
        return alcance;
    }

    public void setAlcance(Alcance alcance) {
        this.alcance = alcance;
    }

    public boolean isUsada() {
        return usada;
    }

    public void setUsada() {
        this.usada = true;
    }

    public String getNombreFunIR() {
        return nombreFunIR;
    }

    public void setNombreFunIR(String nombreFunIR) {
        this.nombreFunIR = nombreFunIR;
    }

    public String getPtroRet() {
        return ptroRet;
    }

    public void setPtroRet(String ptroRet) {
        this.ptroRet = ptroRet;
    }

    public String getRefIR() {
        return refIR;
    }

    public void setRefIR(String refIR) {
        this.refIR = refIR;
    }

    public String getEtiquetaFin() {
        return etiquetaFin;
    }

    public void setEtiquetaFin(String etiquetaFin) {
        this.etiquetaFin = etiquetaFin;
    }

    /**
     * Obtener la cantidad de argumentos no opcionales de la declaración.
     * Usado para validar el pasaje de parámetros.
     */
    public int getCantArgsObligatorios() {
        int cant = 0;
        for (Param p : params) {
            if (p instanceof ParamDef) {
                // Se encontró una variable con parámetro opcional. A partir de acá son todos opcionales.
                break;
            }
            cant++;
        }
        return cant;
    }

    @Override
    public void accept(Visitor v) throws ExcepcionVisitor {
        v.visit(this);
    }

    @Override
    public DecFun accept(Transformer t) throws ExcepcionTransformer {
        return t.transform(this);
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s %s()\\n<%s>", getClass().getSimpleName(),
                getIdent().getNombre(), getTipo());
    }
}
