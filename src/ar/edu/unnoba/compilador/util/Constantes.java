package ar.edu.unnoba.compilador.util;

/** Constantes de configuración general del compilador. */
public class Constantes {
    /** Mostrar el proceso de creación de nodos en el parser y los transformers. */
    public static final boolean mostrarCreacionNodos = false;

    /** Formato de las imágenes de los AST exportada por Graphviz. */
    public static final String formatoImgAst = "png";

    /** Generar comentarios en el código IR. */
    public static final boolean conComentarios = true;

    /** A partir de que columna intentar colocar los comentarios en las líneas. */
    public static final int columnaComent = 60;

    /** Cuánto espacio dejar como mínimo entre el código y el comentario. */
    public static final int espacioMinComent = 1;

    /** Si mostrar stack trace de las excepciones o sólo su descripción */
    public static final boolean mostrarExcepcionesDetalladas = false;
}
