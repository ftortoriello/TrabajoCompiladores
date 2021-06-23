package ar.edu.unnoba.compilador.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/* Clase utilizada por los generadores de alcances para generar
 * nombres normalizados y únicos para variables y funciones de IR.
 */
public class Normalizador {

    private static final Map<String, Long> etiquetas = new HashMap<>();
    private static final Map<String, Long> varGbls = new HashMap<>();
    private static final Map<String, Long> varLcls = new HashMap<>();
    private static final Map<String, Long> varRefs = new HashMap<>();
    private static final Map<String, Long> funciones = new HashMap<>();

    private Normalizador() {
    }

    private static Long getIdMapa(Map<String, Long> mapa, String clave) {
        Long id = mapa.get(clave);
        if (id == null) {
            id = 1L;
        } else {
            id++;
        }
        mapa.put(clave, id);
        return id;
    }

    public static long getIdVarGbl(String nombre) {
        return getIdMapa(varGbls, nombre);
    }

    public static long getIdVarLcl(String nombre) {
        return getIdMapa(varLcls, nombre);
    }

    public static long getIdVarRef(String nombre) {
        return getIdMapa(varRefs, nombre);
    }

    public static long getIdFuncion(String nombre) {
        return getIdMapa(funciones, nombre);
    }

    public static long getIdEtiqueta(String nombre) {
        return getIdMapa(etiquetas, nombre);
    }

    // Devuelve una cadena normalizada
    public static String normalizar(String cadena) {
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    /* Funciones para generar nombres nuevos normalizados únicos */

    // Función
    public static String crearNomFun(String nombreOrig) {
        String nombreNormalizado = normalizar(nombreOrig);
        return String.format("@%s.%s", nombreNormalizado, getIdFuncion(nombreNormalizado));
    }

    // Variable global tipo puntero de IR (gralmente. usado para el atrib. nombreIR)
    public static String crearNomPtroGbl(String nombreOrig) {
        String nombreNormalizado = normalizar(nombreOrig);
        return String.format("@ptro.%s.%s", nombreNormalizado, getIdVarGbl(nombreNormalizado));
    }

    // Variable local tipo puntero de IR (gralmente. usado para el atrib. nombreIR)
    public static String crearNomPtroLcl(String nombreOrig) {
        String nombreNormalizado = normalizar(nombreOrig);
        return String.format("%%ptro.%s.%s", nombreNormalizado, getIdVarLcl(nombreNormalizado));
    }

    // Variable que contenga el valor referenciado por un puntero
    // (gralmente. usado para el atrib refIR)
    public static String crearNomRef(String nombreOrig) {
        return String.format("%%ref.%s.%s", nombreOrig, getIdVarRef(nombreOrig));
    }

    // Etiqueta de LLVM IR
    public static String crearNomEtiqueta(String nombreOrig) {
        return String.format("%s.%s", nombreOrig, getIdEtiqueta(nombreOrig));
    }

}
