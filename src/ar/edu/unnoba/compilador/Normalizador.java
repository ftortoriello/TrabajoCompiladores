package ar.edu.unnoba.compilador;

import java.text.Normalizer;

/* Clase utilizada por los generadores de alcances para generar
 * nombres normalizados y únicos para variables y funciones de IR.
 */
public class Normalizador {

    private long idVarGlobal = 0;
    private long idVarLocal = 0;
    private long idFuncion = 0;

    public Normalizador() {
    }

    public long getIdVarGlobal() {
        idVarGlobal += 1;
        return idVarGlobal;
    }

    public long getIdVarLocal() {
        idVarLocal += 1;
        return idVarLocal;
    }

    public long getIdFuncion() {
        idFuncion += 1;
        return idFuncion;
    }

    // Devuelve una cadena normalizada
    public String normalizar(String cadena) {
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    // TODO: asignarles un nro. a las funciones/variables sólo si es necesario (o sea si chocan con otra)

    // Devuelve el nombre de la función preparado para usar en IR
    public String getNombreFuncion(String nombreFun) {
        return String.format("%s_%s", normalizar(nombreFun), getIdFuncion());
    }

    // Genera un nombre normalizado y único para una nueva variable global de IR
    public String getNombreVarGlobal(String nombreVar) {
        return String.format("@g_%s_%s", normalizar(nombreVar), getIdVarGlobal());
    }

    // Genera un nombre normalizado y único para una nueva variable local de IR
    public String getNombreVarLocal(String nombreVar) {
        return String.format("%%%s_%s", normalizar(nombreVar), getIdVarLocal());
    }

}
