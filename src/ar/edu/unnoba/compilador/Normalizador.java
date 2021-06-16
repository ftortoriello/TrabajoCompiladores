package ar.edu.unnoba.compilador;

import java.text.Normalizer;

/* Clase utilizada por los generadores de alcances para generar
 * nombres normalizados y únicos para variables y funciones de IR.
 */
public class Normalizador {

    private static long idVarGbl = 0;
    private static long idVarLcl = 0;
    private static long idVarAux = 0;
    private static long idFuncion = 0;

    public Normalizador() {
    }

    public static long getIdVarGbl() {
        idVarGbl += 1;
        return idVarGbl;
    }

    public static long getIdVarLcl() {
        idVarLcl += 1;
        return idVarLcl;
    }

    public static long getIdVarAux() {
        idVarAux += 1;
        return idVarAux;
    }

    public static long getIdFuncion() {
        idFuncion += 1;
        return idFuncion;
    }

    // Devuelve una cadena normalizada
    public static String normalizar(String cadena) {
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    // TODO: asignarles un nro. a las funciones/variables sólo si es necesario (o sea si chocan con otra)

    // Devuelve el nombre de la función preparado para usar en IR
    public static String getNvoNomFun(String nombreFun) {
        return String.format("%s.%s", normalizar(nombreFun), getIdFuncion());
    }

    // Genera un nombre normalizado y único para una nueva variable global de IR
    public static String getNvoNomVarGbl(String nombreVar) {
        return String.format("@g.%s.%s", normalizar(nombreVar), getIdVarGbl());
    }

    // Genera un nombre normalizado y único para una nueva variable local de IR
    public static String getNvoNomVarLcl(String nombreVar) {
        return String.format("%%%s.%s", normalizar(nombreVar), getIdVarLcl());
    }

    // Genera un nombre para una variable auxiliar local
    public static String getNvoNomVarAux(String nombreVar) {
        return String.format("%%aux.%s.%s", nombreVar, getIdVarAux());
    }

}
