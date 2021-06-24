package ar.edu.unnoba.compilador.util;

/* Clase auxiliar usada por GeneradorDeCodigo que se encarga de anexar instrucciones IR al código,
 * para abstraernos del formato y la sintaxis de las instrucciones. */

public class GenerarIR {
    // Constante que indica si imprimir o no los comentarios del recorrido del visitor.
    private final boolean conComentariosVisitor = false;
    // Similar, para indicar si se agregan comentarios a la derecha de las líneas.
    private final boolean conComentariosLinea = false;
    // A partir de que columna intentar colocar los comentarios en las líneas
    private final int columnaComent = 70;

    // Sobre esta variable vamos anexando lo que va a ser el resultado final en IR
    private final StringBuilder sbCodigo = new StringBuilder();

    private String comentLinea = null;

    public String getCodigo() {
        return sbCodigo.toString();
    }

    public void setComentLinea(String comentLinea) {
        this.comentLinea = comentLinea;
    }

    /*** Funciones auxiliares ***/

    /* Retorna como pasar un string por parámetro. Usado por printf y scanf */
    private String getParamString(String cadena, int longitud) {
        return String.format("i8* getelementptr ([%1$d x i8], [%1$d x i8]* %2$s, i64 0, i64 0)",
                longitud, cadena);
    }

    private String print(String parametros) {
        // Generamos acá la referencia al resultado porque no la usamos
        return String.format("%s = call i32 (i8*, ...) @printf(%s)",
                Normalizador.crearNomRef("ret_printf"), parametros);
    }

    private String scan(String parametros) {
        return String.format("%s = call i32 (i8*, ...) @scanf(%s)",
                Normalizador.crearNomRef("ret_scanf"), parametros);
    }

    /* Extender de un tipo a otro */
    private String ext(String resultado, String instruccion, String valor,
                       String tipoOrigen, String tipoDestino) {
        return String.format("%s = %s %s %s to %s",
                resultado, instruccion, valor, tipoOrigen, tipoDestino);
    }

    /*** Funciones públicas ***/

    /* Agrega una línea de código */
    public void codigo(String cod, boolean indentado) {
        if (indentado) sbCodigo.append("\t");
        sbCodigo.append(cod);

        if (conComentariosLinea && comentLinea != null) {
            int cantEspacios = columnaComent - cod.length();
            if (cantEspacios < 1) cantEspacios = 1;
            // alinear
            sbCodigo.append(" ".repeat(cantEspacios));
            // agregar comentario
            sbCodigo.append("; ").append(comentLinea);

            // no generar el mismo comentario en la próxima línea
            comentLinea = null;
        }

        sbCodigo.append("\n");
    }

    /* Agregar una línea de código general indentado */
    public void codigo(String codigo) {
        codigo(codigo, true);
    }

    /* Etiqueta; antes se agrega un salto de línea */
    public void etiqueta(String nombreEtiqueta) {
        codigo(String.format("\n%s:", nombreEtiqueta), false);
    }

    /* Si se pidió mostrar comentarios para depurar la generación de código, agregar un comentario */
    public void coment(String comentario) {
        if (conComentariosVisitor) {
            codigo(String.format("\n; %s", comentario), false);
        }
    }

    /* Salto incondicional */
    public void salto(String etiquetaDestino) {
        codigo(String.format("br label %%%s", etiquetaDestino));
    }

    /* Salto condicional */
    public void salto(String cond, String etiVerdadero, String etiFalso) {
        codigo(String.format("br i1 %s, label %%%s, label %%%s",
                cond, etiVerdadero, etiFalso));
    }

    /* Asignar resultado de una operación unaria */
    public void asig(String resultado, String instruccion, String tipo, String operando) {
        codigo(String.format("%s = %s %s %s", resultado, instruccion, tipo, operando));
    }

    /* Asignar resultado de una operación binaria */
    public void asig(String resultado, String instruccion, String tipo, String op1, String op2) {
        codigo(String.format("%s = %s %s %s, %s", resultado, instruccion, tipo, op1, op2));
    }

    /* Crear una variable global */
    public void global(String nombre, String tipo, String valor) {
        asig(nombre, "global", tipo, valor);
    }

    /* Reservar memoria para un registro */
    public void alloca(String nombre, String tipo) {
        codigo(String.format("%s = alloca %s", nombre, tipo));
    }

    /* Leer de memoria un valor y almacenarlo en un registro */
    public void load(String resultado, String tipo, String ptro) {
        codigo(String.format("%1$s = load %2$s, %2$s* %3$s", resultado, tipo, ptro));
    }

    /* Almacenar un valor en memoria */
    public void store(String destino, String tipo, String origen) {
        codigo(String.format("store %2$s %1$s, %2$s* %3$s", origen, tipo, destino));
    }

    public void suma(String resultado, String tipo, String op1, String op2) {
        final String instruccion = tipo.startsWith("i") ? "add" : "fadd";
        asig(resultado, instruccion, tipo, op1, op2);
    }

    /* Extender a i32, manteniendo el signo */
    public void sext(String resultado, String tipoOrigen, String valor) {
        codigo(ext(resultado, "sext", valor, tipoOrigen, "i32"));
    }

    /* Extender a i32, sin mantener el signo */
    public void zext(String resultado, String tipoOrigen, String valor) {
        codigo(ext(resultado, "zext", valor, tipoOrigen, "i32"));
    }

    public void cmp(String tipoCmp, String resultado, String tipoOp, String op1, String op2) {
        // Establecer si hacer una comparación entre enteros o flotantes
        final String instr = tipoOp.startsWith("i") ? "icmp" : "fcmp";
        asig(resultado, instr + " " + tipoCmp, tipoOp, op1, op2);
    }

    /* Comparar si dos números son iguales, y saltar a la etiqueta correspondiente. */
    public void saltoIgual(String resultado, String tipo, String op1, String op2,
                           String saltoVerd, String saltoFalso) {
        cmp("eq", resultado, tipo, op1, op2);
        salto(resultado, saltoVerd, saltoFalso);
    }

    /* Invocación a printf sin parámetros */
    public void print(String cadena, int longitud) {
        codigo(print(getParamString(cadena, longitud)));
    }

    /* Invocación a printf con un parámetro */
    public void print(String cadena, int longitud, String tipo, String ptro) {
        codigo(print(getParamString(cadena, longitud) +
                      String.format(", %s %s", tipo, ptro)));
    }

    /* Invocación a scanf */
    public void scan(String cadena, int longitud, String tipoRet, String ptro) {
        codigo(scan(getParamString(cadena, longitud) +
                String.format(", %s* %s", tipoRet, ptro)));
    }

    /* Abrir bloque con la definición de una función. El @ ya tiene que estar en el nombre. */
    public void defFuncion(String nombre, String tipoRet, String params) {
        codigo(String.format("define %s %s(%s) {", tipoRet, nombre, params), false);
    }

    /* Cerrar bloque de definición de función */
    public void cierreBloque() {
        codigo("}\n\n", false);
    }

    /* Retorno de una función void */
    public void ret() {
        codigo("ret void");
    }

    /* Retorno de una función */
    public void ret(String tipo, String valor) {
        codigo(String.format("ret %s %s", tipo, valor));
    }

    /* Invocación a una función de tipo void */
    public void invocacion(String nombre, String parametros) {
        codigo(String.format("call void %s(%s)", nombre, parametros));
    }

    /* Invocación a una función */
    public void invocacion(String nombre, String tipo, String resultado, String parametros) {
        codigo(String.format("%s = call %s %s(%s)", resultado, tipo, nombre, parametros));
    }

    /* Definición de una variable global conteniendo un string */
    public void cadena(String nombre, String cadena, int longitud) {
        codigo(String.format("%s = private constant [%d x i8] c\"%s\"\n", nombre, longitud, cadena));
    }
}
