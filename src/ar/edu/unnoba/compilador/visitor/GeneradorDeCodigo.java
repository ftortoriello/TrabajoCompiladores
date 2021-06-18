package ar.edu.unnoba.compilador.visitor;

import ar.edu.unnoba.compilador.Normalizador;
import ar.edu.unnoba.compilador.ast.base.*;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.ast.expresiones.Tipo;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.OperacionBinaria;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.aritmeticas.OperacionBinariaAritmetica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.logicas.OperacionBinariaLogica;
import ar.edu.unnoba.compilador.ast.expresiones.binarias.relaciones.Relacion;
import ar.edu.unnoba.compilador.ast.expresiones.unarias.OperacionUnaria;
import ar.edu.unnoba.compilador.ast.expresiones.valor.*;
import ar.edu.unnoba.compilador.ast.sentencias.Asignacion;
import ar.edu.unnoba.compilador.ast.sentencias.control.Continuar;
import ar.edu.unnoba.compilador.ast.sentencias.control.Retorno;
import ar.edu.unnoba.compilador.ast.sentencias.control.Salir;
import ar.edu.unnoba.compilador.ast.sentencias.declaracion.*;
import ar.edu.unnoba.compilador.ast.sentencias.iteracion.Mientras;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntonces;
import ar.edu.unnoba.compilador.ast.sentencias.seleccion.SiEntoncesSino;
import jflex.base.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneradorDeCodigo extends Visitor {
    // TODO: ver excepciones, las que van acá serían ExcepcionDeCompilacion (aunque hacen exactamente lo mismo)
    // TODO: conversiones implícitas

    private StringBuilder codigo;
    private String nombreArchivoFuente;

    private Map<String, SimboloFuncion> tablaFunciones;

    // Mapa para relacionar nuestros tipos con los del IR (y además definir valores por defecto)
    private final Map<Tipo, Pair<String, String>> TIPO_IR = new HashMap<>() {{
        put(Tipo.BOOLEAN, new Pair<>("i1", "0"));
        put(Tipo.INTEGER, new Pair<>("i32", "0"));
        put(Tipo.FLOAT, new Pair<>("float", "0.0"));
    }};

    /*** Funciones auxiliares ***/

    public static boolean targetEsWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /* Función para no tener fija la arquitectura y SO destino.
     * El triple se puede sacar de  "llvm-config --host-target"
     * o "clang -print-target-triple", pero el datalayout no se.
     * Compilar un programa básico en C a IR y fijarse.
     */
    private static String getHostTarget() throws IOException, InterruptedException {
        String datalayout = null;
        String triple = null;

        PrintWriter pw = new PrintWriter(new FileWriter("void.c"));
        pw.println("int main() {}");
        pw.close();
        Process clang = Runtime.getRuntime().exec("clang -emit-llvm -S -o - void.c");
        BufferedReader reader = new BufferedReader(new InputStreamReader(clang.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null && (datalayout == null || triple == null)) {
            if (line.startsWith("target datalayout")) {
                datalayout = line;
            } else if (line.startsWith("target triple")) {
                triple = line;
            }
        }
        clang.waitFor();
        // borrar archivo C temporal
        File file = new File("void.c");
        file.delete();
        if (datalayout == null || triple == null) {
            // falló algo... tirar excepción para usar datos predeterminados
            throw new IOException("No se pudieron obtener los datos del host");
        }
        return datalayout + "\n" + triple;
    }

    // Genera un nombre único para una nueva etiqueta
    private String getNuevaEtiqueta(String nombreEtiqueta) {
        return String.format("e_%s_%s", nombreEtiqueta, getID());
    }

    private void imprimirEtiqueta(String nombreEtiqueta) {
        codigo.append(String.format("\n%s:\n", nombreEtiqueta));
    }

    private void grarCodSaltoInc(String etiquetaDestino) {
        codigo.append("\t" + String.format("br label %%%s\n", etiquetaDestino));
    }

    private void grarCodSaltoCond(String cond, String etiquetaTrue, String etiquetaFalse) {
        codigo.append(String.format("\tbr i1 %s, label %%%s, label %%%s\n", cond, etiquetaTrue, etiquetaFalse));
    }

    private String grarStrParams(ArrayList<Param> arrParams) {
        // Genera una lista de parámetros de acuerdo a lo requerido por IR
        StringBuilder strParams = new StringBuilder();

        int cantParams = arrParams.size();

        for (int i = 0; i < cantParams; i++) {
            SimboloVariable simboloArg = (SimboloVariable) arrParams.get(i).getIdent();
            String tipoRetornoArg = TIPO_IR.get(simboloArg.getTipo()).fst;
            String argNombreIR = simboloArg.getNombreIR();

            // Para separar los argumentos mediante comas, excepto el final
            String sep = i != cantParams - 1 ? ", " : "";

            // Añado el argumento a la lista
            strParams.append("\t" + String.format("%s %s%s", tipoRetornoArg, argNombreIR, sep));
        }

        return strParams.toString();
    }

    /*** Función principal ***/
    public String generarCodigo(Programa p, String nombreArchivoFuente) throws ExcepcionDeAlcance {
        this.nombreArchivoFuente = nombreArchivoFuente;
        tablaFunciones = p.getTablaFunciones();

        super.procesar(p);
        return codigo.toString();
    }


    /**** Visitors ***/

    /* Base */

    @Override
    public void visit(Programa p) throws ExcepcionDeAlcance {
        codigo = new StringBuilder();
        codigo.append(String.format("; Programa: %s\n", p.getNombre()))
              .append(String.format("source_filename = \"%s\"\n", nombreArchivoFuente));

        try {
            codigo.append(getHostTarget());
        } catch (IOException | InterruptedException e) {
            // algo falló... dejarlo hardcodeado
            if (targetEsWindows()) {
                // Target Bruno
                codigo.append("target datalayout = \"e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n")
                      .append("target triple = \"x86_64-pc-windows-msvc19.28.29335\"\n");
            } else {
                // Target Franco
                codigo.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128\"\n")
                      .append("target triple = \"x86_64-pc-linux-gnu\"\n");
            }
        }

        codigo.append("\n")
              .append("declare i32 @puts(i8*)\n")
              .append("declare i32 @printf(i8*, ...)\n")
              .append("\n")
              .append("@.true = private constant[4 x i8] c\".T.\\00\"\n")
              .append("@.false = private constant[4 x i8] c\".F.\\00\"\n")
              .append("\n")
              .append("@.integer = private constant [4 x i8] c\"%d\\0A\\00\"\n")
              .append("@.float = private constant [4 x i8] c\"%f\\0A\\00\"\n");

        super.visit(p);
    }
    
    @Override
    public void visit(Encabezado e) throws ExcepcionDeAlcance {
        super.visit(e);
    }

    @Override
    public void visit(Bloque b) throws ExcepcionDeAlcance {
        if (b.esProgramaPrincipal()) {
            codigo.append("\ndefine i32 @main(i32, i8**) {\n");
            super.visit(b);
            codigo.append("\tret i32 0\n");
            codigo.append("}\n\n");
        } else {
            super.visit(b);
        }
    }


    /* Sentencia de asignación */

    @Override
    public void visit(Asignacion asig) throws ExcepcionDeAlcance {
        SimboloVariable svDestino = (SimboloVariable) asig.getIdent();

        // Anexar declaración de referencias necesarias para la parte derecha de la asig.
        asig.getExpresion().accept(this);

        // origen va a contener la referencia al valor de la expresión
        String origen = asig.getExpresion().getRefIR();
        String tipoOrigen = TIPO_IR.get(asig.getExpresion().getTipo()).fst;
        String destino = svDestino.getNombreIR();
        String tipoDestino = TIPO_IR.get(svDestino.getTipo()).fst;
        // tipoOrigen y tipoDestino deberían ser iguales, pero lo dejo así para detectar algún error
        // y de paso usar los nombres de las variables para que quede un poco más claro lo que se hace

        codigo.append(String.format("\t; visit(Asignacion sobre %s)\n", svDestino.getNombre()));

        codigo.append(String.format("\tstore %1$s %2$s, %3$s* %4$s\t; %4$s = %2$s\n",
                tipoOrigen, origen, tipoDestino, destino));
    }


    /* Sentencias de declaración */

    @Override
    public void visit(DecVar dv) throws ExcepcionDeAlcance {
        // Genera la declaración de una variable que no fue inicializada

        SimboloVariable sv = (SimboloVariable) dv.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        Boolean esGlobal = sv.getEsGlobal();
        String valorIR = TIPO_IR.get(dv.getTipo()).snd;

        // Mostrar comentario con la declaración en el lenguaje original
        codigo.append(String.format("\n\t; DecVar: variable %s is %s = %s\n",
                sv.getNombre(), sv.getTipo(), valorIR));

        if (esGlobal) {
            codigo.append(String.format("\t%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
            codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        }

        codigo.append("\n");
    }

    @Override
    public void visit(DecVarIni dvi) throws ExcepcionDeAlcance {
        // Genera la declaración de una variable que sí fue inicializada

        SimboloVariable sv = (SimboloVariable) dvi.getIdent();

        // Parámetros que necesito para declarar la variable
        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        String valorIR;

        if (sv.getEsGlobal()) {
            // TODO
            // Tengo que armar una función que me retorne el valor para poder asignarla
            // valorIR = el return de la funcion que hay que crear
            valorIR = "globalIni";

            // Mostrar comentario con la declaración en el lenguaje original
            codigo.append(String.format("\t; DecVarIni: variable %s is %s = %s\n",
                    sv.getNombre(), sv.getTipo(), valorIR));

            codigo.append(String.format("\t%s = global %s %s\n", nombreIR, tipoIR, valorIR));
        } else {
            // Visito a la expresión para generar la declaración de referencias necesarias
            dvi.getExpresion().accept(this);

            // El refIR con el valor de la expresión ya viene seteado gracias a la visita anterior
            valorIR = dvi.getExpresion().getRefIR();

            // Mostrar comentario con la declaración en el lenguaje original
            codigo.append(String.format("\t; DecVarIni: variable %s is %s = %s\n",
                    sv.getNombre(), sv.getTipo(), valorIR));

            codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
            codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\n", nombreIR, tipoIR, valorIR));
        }
    }

    @Override
    public void visit(DecFun df) throws ExcepcionDeAlcance {
        SimboloFuncion simboloFun = tablaFunciones.get(df.getNombre());

        // Elementos que necesito para definir la función: tipo de retorno, nombre, parámetros y el cuerpo
        String funTipoRetIR = TIPO_IR.get(simboloFun.getTipo()).fst;
        String funNombreIR = simboloFun.getNombreIR();

        // Formatear la lista de parámetros de acuerdo a lo requerido por IR
        String params = grarStrParams(df.getParams());
        codigo.append(String.format("\ndefine %s @%s(%s) {\n", funTipoRetIR, funNombreIR, params));

        // Anexar referencias de los parámetros al principio de la función
        for (Param param : df.getParams()) {
            param.accept(this);
        }

        // Anexar cuerpo de la función y cerrar
        df.getBloque().accept(this);
        codigo.append("}\n\n");
    }

    @Override
    public void visit(Param p) throws ExcepcionDeAlcance {
        /* Para poder utilizar el parámetro creo una variable auxiliar,
         * para la cual genero un nombreIR y un refIR, que pisan al que
         * viene en el objeto SimboloVariable del parámetro. Después
         * guardo el valor que viene en el parámetro en esta "nueva" var.
         * Esto supone que el pasaje es por valor y no por referencia.
         */

        SimboloVariable sv = (SimboloVariable) p.getIdent();

        // Guardo el nombre formal acá para poder extraer el valor, después lo piso
        String nombreFormal = sv.getNombreIR();
        String refIR = Normalizador.crearNomPtroLcl("ref");
        String nombreIR = Normalizador.crearNomPtroLcl("param");
        String tipoIR = TIPO_IR.get(p.getTipo()).fst;

        sv.setRefIR(refIR);
        sv.setNombreIR(nombreIR);

        codigo.append(String.format("\t; visit(Param %s)\n", sv.getNombre()));
        codigo.append(String.format("\t%s = alloca %s\n", nombreIR, tipoIR));
        codigo.append(String.format("\tstore %2$s %3$s, %2$s* %1$s\t; %1$s = %3$s\n", nombreIR, tipoIR, nombreFormal));
    }

    @Override
    public void visit(ParamDef pd) throws ExcepcionDeAlcance {
        // TODO
        codigo.append("\t; visit(ParamDef) sin implementar");
    }


    /* Sentencias de selección */

    @Override
    public void visit(SiEntonces se) throws ExcepcionDeAlcance {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiFin = getNuevaEtiqueta("fin_if");

        codigo.append("; visit(SiEntonces\n");

        // Salto condicional
        se.getCondicion().accept(this);
        String refCond = se.getCondicion().getRefIR();
        grarCodSaltoCond(refCond, etiBlqThen, etiFin);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        se.getBloqueSiEntonces().accept(this);
        grarCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);
    }

    @Override
    public void visit(SiEntoncesSino ses) throws ExcepcionDeAlcance {
        String etiBlqThen = getNuevaEtiqueta("blq_then");
        String etiBlqElse = getNuevaEtiqueta("blq_else");
        String etiFin = getNuevaEtiqueta("fin_if");

        codigo.append("; visit(SiEntoncesSino)\n");

        // Salto condicional
        ses.getCondicion().accept(this);
        String refCond = ses.getCondicion().getRefIR();
        grarCodSaltoCond(refCond, etiBlqThen, etiBlqElse);

        // Caso true
        imprimirEtiqueta(etiBlqThen);
        ses.getBloqueSiEntonces().accept(this);
        grarCodSaltoInc(etiFin);

        // Caso false
        imprimirEtiqueta(etiBlqElse);
        ses.getBloqueSino().accept(this);
        grarCodSaltoInc(etiFin);

        // Fin if
        imprimirEtiqueta(etiFin);
    }


    /* Sentencias de iteración */

    @Override
    public void visit(Mientras m) throws ExcepcionDeAlcance {
        String etiInicioWhile = getNuevaEtiqueta("inicio_while");
        String etiBucleWhile = getNuevaEtiqueta("bucle_while");
        String etiFinWhile = getNuevaEtiqueta("fin_while");

        codigo.append("; visit(Mientras)\n");
        grarCodSaltoInc(etiInicioWhile);
        imprimirEtiqueta(etiInicioWhile);

        // Generar ref. al resultado de la condición
        m.getCondicion().accept(this);
        String refCond = m.getCondicion().getRefIR();

        // Se evalúa la condición, si es verdadera se salta al bucle y si es falsa al fin
        grarCodSaltoCond(refCond, etiBucleWhile, etiFinWhile);
        imprimirEtiqueta(etiBucleWhile);

        // Generar cuerpo del while
        m.getBloqueSentencias().accept(this);

        // Ejecutado el cuerpo, se evalúa de nuevo la condición inicial
        grarCodSaltoInc(etiInicioWhile);

        imprimirEtiqueta(etiFinWhile);
    }


    /* Sentencias de control */

    @Override
    public void visit(Continuar c) throws ExcepcionDeAlcance {
        super.visit(c);
    }

    @Override
    public void visit(Retorno r) throws ExcepcionDeAlcance {
        // Generar refIR para la expresión de retorno
        r.getExpr().accept(this);

        String tipoRetorno = TIPO_IR.get(r.getExpr().getTipo()).fst;
        String refRetorno = r.getExpr().getRefIR();

        codigo.append(String.format("\tret %s %s\n", tipoRetorno, refRetorno));
    }

    @Override
    public void visit(Salir s) throws ExcepcionDeAlcance {
        super.visit(s);
    }


    /* Operaciones */

    @Override
    public void visit(OperacionBinaria ob) throws ExcepcionDeAlcance {
        // Defino un nombre auxiliar con el cual puedo referenciar el valor de la expr.
        String refIR = Normalizador.crearNomRef("ob");
        ob.setRefIR(refIR);

        // El padre visita a las exprs. izq. y der. para generar la declaración de referencias
        super.visit(ob);

        String refIzqIR = ob.getIzquierda().getRefIR();
        String refDerIR = ob.getDerecha().getRefIR();
        String instIR = ob.getInstruccionIR();
        String tipoIR = TIPO_IR.get(ob.getIzquierda().getTipo()).fst;
        String operadorParser = ob.getNombre();

        if (ob instanceof OperacionBinariaAritmetica) {
            // Por ej.: %aux.ob.11 = add i32 %aux.sv.9, %aux.ref.10 ; %aux.ob.11 = %aux.sv.9 + %aux.ref.10
            codigo.append(String.format("\t%1$s = %2$s %3$s %4$s, %5$s\t; %1$s = %4$s %6$s %5$s\n",
                    refIR, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        } else if (ob instanceof Relacion) {
            String tipoCmp = ((Relacion)ob).getTipoCmp();
            // Por ej.: %aux.ob.15 = icmp sgt i32 %aux.sv.13, %aux.sv.14 ; %aux.sv.13 > %aux.sv.14
            codigo.append(String.format("\t%1$s = %2$s %3$s %4$s %5$s, %6$s ; %5$s %7$s %6$s\n",
                    refIR, tipoCmp, instIR, tipoIR, refIzqIR, refDerIR, operadorParser));
        } else if (ob instanceof OperacionBinariaLogica) {
            codigo.append("\t; OperacionBinaria -> OpBinLog sin implementar\n");
        }
    }

    @Override
    public void visit(OperacionUnaria ou) throws ExcepcionDeAlcance {
        super.visit(ou);
    }


    /* Valores */

    @Override
    public void visit(Literal lit) throws ExcepcionDeAlcance {
        // Este visitor genere una variable auxiliar para utilizar los valores literales
        // Como alternativa a generar la variable, podríamos guardar directamente el valor
        // pero de esta manera queda más uniforme con la forma en la que hacemos lo otro.

        // TODO ver acá el tema ese de truncar los valores

        String refIR = Normalizador.crearNomRef("lit");
        lit.setRefIR(refIR);

        Tipo tipoParser = lit.getTipo();
        String tipoIR = TIPO_IR.get(tipoParser).fst;
        String valorParser = lit.getValor();
        String valorIR;

        if (tipoParser == Tipo.INTEGER) {
            valorIR = valorParser;
        } else if (tipoParser == Tipo.FLOAT) {
            valorIR = String.valueOf(valorParser);
        } else if (tipoParser == Tipo.BOOLEAN) {
            valorIR = valorParser.equals("false") ? "0" : "1";
        } else {
            throw new ExcepcionDeAlcance("Valor de tipo inesperado: " + lit.getTipo());
        }

        codigo.append(String.format("\t; visit(Literal %s)\n", valorParser));
        // Hack para generar referencias a valores en una línea (le sumo 0 al valor que quiero guardar)
        codigo.append(String.format("\t%s = add %s %s, 0\n", refIR, tipoIR, valorIR));
    }

    @Override
    public void visit(Identificador ident) {
        // Genera el store sobre un refIR para poder acceder a una variable

        // En esta etapa este Identificador va a ser siempre un SimboloVariable. Tengo que utilizarlo así porque
        // si creo visit(SimboloVariable) da muchos problemas (por ej. se rompe el graficado del AST)
        SimboloVariable sv = (SimboloVariable) ident;

        String nombreIR = sv.getNombreIR();
        String tipoIR = TIPO_IR.get(sv.getTipo()).fst;
        String refIR = Normalizador.crearNomRef("sv");
        sv.setRefIR(refIR);

        codigo.append(String.format("\t; visit(Identificador %s)\n", sv.getNombre()));
        codigo.append(String.format("\t%1$s = load %2$s, %2$s* %3$s\n", refIR, tipoIR, nombreIR));
    }

    @Override
    public void visit(InvocacionFuncion i) throws ExcepcionDeAlcance {
        super.visit(i);

        // TODO
        codigo.append(String.format("\t; Invocación a %s()\n", i.getNombre()));
        if (i.getEsPredefinida()) {

        }
    }
}
