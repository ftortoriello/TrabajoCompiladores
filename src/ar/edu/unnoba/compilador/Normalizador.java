package ar.edu.unnoba.compilador;

import java.text.Normalizer;

/* Clase utilizada por los generadores de alcances para generar
 * nombres normalizados y únicos para variables y funciones de IR.
 */
public class Normalizador {

    private static long idVarGlobal = 0;
    private static long idVarLocal = 0;
    private static long idVarTemp = 0;
    private static long idFuncion = 0;

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

    public static long getIdVarTemp() {
        idVarTemp += 1;
        return idVarTemp;
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
    public String getNuevoNomFun(String nombreFun) {
        return String.format("%s.%s", normalizar(nombreFun), getIdFuncion());
    }

    // Genera un nombre normalizado y único para una nueva variable global de IR
    public String getNuevoNomVarGlobal(String nombreVar) {
        return String.format("@g.%s.%s", normalizar(nombreVar), getIdVarGlobal());
    }

    // Genera un nombre normalizado y único para una nueva variable local de IR
    public String getNuevoNomVarLocal(String nombreVar) {
        return String.format("%%%s.%s", normalizar(nombreVar), getIdVarLocal());
    }

    // Genera un nombre para una variable auxiliar
    public String getNuevoNomVarAux() {
        return String.format("%%temp.%s", getIdVarTemp());
    }

}
