package ar.edu.unnoba.compilador;

import java.text.Normalizer;

/* Clase utilizada por los generadores de alcances para generar
 * nombres normalizados y únicos para variables y funciones de IR.
 */
public class Normalizador {

    private static long idVarGbl = 0;
    private static long idVarLcl = 0;
    private static long idVarRef = 0;
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

    public static long getIdVarRef() {
        idVarRef += 1;
        return idVarRef;
    }

    public static long getIdFuncion() {
        idFuncion += 1;
        return idFuncion;
    }

    // Devuelve una cadena normalizada
    public static String normalizar(String cadena) {
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    // TODO: asignarles un nro. a las funciones/variables sólo si es necesario
    // O sea si chocan con otra después de ser normalizados

    // Genera un nombre para una función
    public static String crearNomFun(String nombreOrigFun) {
        return String.format("@%s.%s", normalizar(nombreOrigFun), getIdFuncion());
    }

    // Genera un nombre para una nueva variable global tipo puntero de IR (gralmente. usado para el atrib. nombreIR)
    public static String crearNomPtroGbl(String nombreOrigVar) {
        return String.format("@ptro.%s.%s", normalizar(nombreOrigVar), getIdVarGbl());
    }

    // Genera un nombre para una nueva variable local tipo puntero de IR (gralmente. usado para el atrib. nombreIR)
    public static String crearNomPtroLcl(String nombreOrigVar) {
        return String.format("%%ptro.%s.%s", normalizar(nombreOrigVar), getIdVarLcl());
    }

    // Genera un nombre para una variable que contenga el valor referenciado
    // por un puntero (gralmente. usado para el atrib refIR)
    public static String crearNomRef(String nombreOrigVar) {
        return String.format("%%ref.%s.%s", nombreOrigVar, getIdVarRef());
    }

}
