package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.sentencias.declaracion.DecFun;

/**
 * Clase para asociar las invocaciones a funciones a sus declaraciones.
 * Luego mediante un Transformer este nodo reemplaza a InvocacionFuncion.
 */
public class SimboloFuncion extends InvocacionFuncion {
    private DecFun declaracion;

    private final String nombreFuncionIR;
    private final String ptroRet;

    public String getNombreFuncionIR() {
        return nombreFuncionIR;
    }

    public SimboloFuncion(DecFun d, String nombreFuncionIR, String ptroRet) {
        super(d.getIdent().getNombre(), d.getIdent().getTipo());
        this.nombreFuncionIR = nombreFuncionIR;
        this.ptroRet = ptroRet;
        this.declaracion = d;
    }

    public DecFun getDeclaracion() {
        return declaracion;
    }

    public void setDeclaracion(DecFun declaracion) {
        this.declaracion = declaracion;
    }

    public String getPtroRet() {
        return ptroRet;
    }
}
