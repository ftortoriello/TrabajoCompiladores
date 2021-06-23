package ar.edu.unnoba.compilador.util;

/* Clase auxiliar usada por GeneradorDeCodigo que se encarga de anexar instrucciones IR al código,
 * para abstraernos del formato. */

public class GenerarIR {
    // Constante que indica si imprimir o no los comentarios del recorrido del visitor.
    private final boolean comentariosVisitor = false;
    // Similar, para indicar si se agregan comentarios a la derecha de las líneas.
    // TODO: Sin uso
    private final boolean comentariosLinea = false;

    // Sobre esta variable vamos anexando lo que va a ser el resultado final en IR
    private StringBuilder sbCodigo;

    public String getCodigo() {
        return sbCodigo.toString();
    }

    /*** Funciones auxiliares ***/

    /* Retorna como pasar un string por parámetro. Usado por printf y scanf */
    private String getParamString(String str, int tamanho) {
        return String.format("i8* getelementptr ([%1$s x i8], [%1$s x i8]* %2$s, i64 0, i64 0)",
                tamanho, str);
    }

    private String printf(String parametros) {
        // Generamos acá la referencia al resultado porque no la usamos
        return String.format("%s = call i32 (i8*, ...) @printf(%s)",
                Normalizador.crearNomRef("ret_printf"), parametros);
    }

    private String scanf(String parametros) {
        return String.format("%s = call i32 (i8*, ...) @scanf(%s)",
                Normalizador.crearNomRef("ret_scanf"), parametros);
    }

    /*** Funciones públicas ***/

    /* Agrega una línea de código */
    public void codigo(String cod, boolean indentado) {
        if (indentado) sbCodigo.append("\t");
        sbCodigo.append(cod).append("\n");
    }

    /* Agregar una línea de código indentado */
    public void codigo(String codigo) {
        codigo(codigo, true);
    }

    /* Generar código con un comentarios alineados, si es posible */
    public void codigo(String codigo, String comentario) {
        int cantEspacios = 70 - codigo.length();

        if (cantEspacios < 1) cantEspacios = 1;

        String espacios = " ".repeat(cantEspacios);
        codigo(codigo + espacios + "; " + comentario);
    }

    /* Etiqueta; antes se agrega un salto de línea */
    public void etiqueta(String nombreEtiqueta) {
        codigo(String.format("\n%s:", nombreEtiqueta), false);
    }

    /* Si se pidió mostrar comentarios para depurar la generación de código, agregar un comentario */
    public void coment(String comentario) {
        if (comentariosVisitor) {
            codigo(String.format("\n; %s", comentario), false);
        }
    }

    /* Salto incondicional */
    public void salto(String etiquetaDestino) {
        codigo(String.format("br label %%%s", etiquetaDestino));
    }

    /* Salto condicional */
    public void salto(String cond, String etiquetaTrue, String etiquetaFalse) {
        codigo(String.format("br i1 %s, label %%%s, label %%%s",
                cond, etiquetaTrue, etiquetaFalse));
    }

    /* Asignar resultado de una operación unaria */
    public void asig(String resultado, String instruccion, String tipo, String operando) {
        codigo(String.format("%s = %s %s %s", resultado, instruccion, tipo, operando));
    }

    /* Asignar resultado de una operación binaria */
    public void asig(String resultado, String instruccion, String tipo, String op1, String op2) {
        codigo(String.format("%s = %s %s %s, %s", resultado, instruccion, tipo, op1, op2));
    }

    /* Reservar memoria para un registro */
    public void alloca(String nombre, String tipo) {
        codigo(String.format("%1 = alloca %2", nombre, tipo));
    }

    /* Comparar si dos enteros son iguales */
    public void icmpEq(String resultado, String op1, String op2) {
        asig(resultado, "icmp eq", "i32", op1, op2);
    }

    /* Invocación a printf sin parámetros */
    public void printf(String str, int tamanho) {
        codigo(printf(getParamString(str, tamanho)));
    }

    /* Invocación a printf con un parámetro */
    // FIXME Creo que quedó un lío con los paréntesis en printf y scanf
    public void printf(String str, int tamanho, String tipo, String ref) {
        codigo(printf(getParamString(str, tamanho) +
                      String.format(", %s %s", tipo, ref)));
    }

    /* Invocación a scanf */
    public void scanf(String str, int tamanho, String tipoLeido, String ptroValorLeido) {
        codigo(scanf(getParamString(str, tamanho) +
                     String.format(", i64 0, i64 0), , %s* %s)")));
    }

    /* Abrir bloque con la definición de una función */
    public void defFuncion(String nombre, String tipoRet) {
        codigo(String.format("define %s @read_%s() {", tipoRet, nombre), false);
    }

}
