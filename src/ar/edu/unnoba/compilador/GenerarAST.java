package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.visitor.*;
import java_cup.runtime.Symbol;

import java.io.*;

public class GenerarAST {
    private static void graficarArbol(String dot, String nombreArchivo) throws IOException, InterruptedException {
        final String formatoImg = "png";
        //final String formatoImg = "svg";

        PrintWriter pw = new PrintWriter(
                new FileWriter(String.format("%s.dot", nombreArchivo)));
        pw.println(dot);
        pw.close();

        String cmd[] = new String[5];
        cmd[0] = "dot";
        cmd[1] = "-T" + formatoImg;
        // entrada
        cmd[2] = nombreArchivo + ".dot";
        // salida
        cmd[3] = "-o";
        cmd[4] = nombreArchivo + "." + formatoImg;
        int exitCode = Util.ejecutar(cmd);
        if (exitCode == 0) {
            System.out.println("Se graficó el AST «" + nombreArchivo + "».");
        } else {
            System.out.println("Ha ocurrido un error al graficar el AST «" + nombreArchivo + "».");
        }
    }

    private static void generarCodigoIR(Programa p, String nombreArchivo) throws IOException, ExcepcionDeAlcance {
        GeneradorDeCodigo gc = new GeneradorDeCodigo();
        PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo));
        pw.println(gc.procesar(p, nombreArchivo ));
        pw.close();
    }

    private static void compilarExe(String nombre) throws IOException, InterruptedException {
        final String nombreExe;
        if (System.getProperty("os.name").startsWith("Windows")) {
            nombreExe = nombre + ".exe";
        } else {
            // Unix
            nombreExe = nombre;
        }

        String[] cmd = new String[4];
        cmd[0] = "clang";
        // entrada
        cmd[1] = nombre + ".ll";
        // salida
        cmd[2] = "-o";
        cmd[3] = nombreExe;
        int exitCode = Util.ejecutar(cmd);
        if (exitCode == 0) {
            System.out.format("Ejecutable generado exitosamente en «%s».\n", nombreExe);
        } else {
            System.out.format("Código de salida %d al generar el ejecutable.\n", exitCode);
        }
    }

    public static void main(String[] args) {
        final String ruta = System.getProperty("user.dir") + "/src/ar/edu/unnoba/compilador/entrada.txt";
        FileReader entrada;
        try {
            entrada = new FileReader(ruta);
        } catch (FileNotFoundException e) {
            System.out.println("No se pudo encontrar el archivo: " + ruta);
            return;
        }

        // TODO: Podríamos dejar todos los archivos generados en otra carpeta, por ej. out, y entrada.txt en la raíz...
        Lexer lexico = new Lexer(entrada);
        @SuppressWarnings("deprecation") Parser parser = new Parser(lexico);
        try {
            Symbol simboloPrograma = parser.parse();
            if (simboloPrograma == null) {
                System.out.println("\nEntrada vacía.");
                return;
            }
            if (!(simboloPrograma.value instanceof Programa)) {
                // Error sintáctico, ya se tendría que haber mostrado el error
                return;
            }

            Programa programa = (Programa) simboloPrograma.value;
            ASTGraphviz graficador;

            // Ejecutar Visitor graficador del árbol sin transformar,
            // y convertirlo a imagen
            graficador = new ASTGraphviz("Árbol de sintaxis abstracta (Conti - Tortoriello)");
            graficarArbol(graficador.procesar(programa),"1_ast-original");

            System.out.println("\nIniciando generador de alcances globales...");
            new GeneradorDeAlcanceGlobal().procesar(programa);

            System.out.println("\nIniciando generador de alcances locales...");
            new GeneradorDeAlcancesLocales().procesar(programa);

            System.out.println("\nIniciando validador de sentencias de control...");
            new VisitorControl().procesar(programa);

            System.out.println("\nIniciando validación y conversión de tipos...");
            new TransformerTipos().procesar(programa);

            System.out.println("\nReescribiendo estructuras when y for...");
            new ConversorDeEstructuras().procesar(programa);

            // Mostrar el árbol transformado
            graficador = new ASTGraphviz("AST con conversión de tipos (Conti - Tortoriello)");
            graficarArbol(graficador.visit(programa),"2_ast-transformado");

            System.out.println("\nIniciando proceso de optimización...");
            new Optimizador().procesar(programa);

            // Mostrar el árbol optimizado
            graficador = new ASTGraphviz("AST optimizado (Conti - Tortoriello)");
            graficarArbol(graficador.visit(programa),"3_ast-optimizado");

            final String nombreArchivo = "4-entrada";

            System.out.println("\nIniciando traducción a código IR...");
            generarCodigoIR(programa, nombreArchivo + ".ll");
            System.out.println("\nTraducción a código IR finalizada");

            System.out.println("\nConvirtiendo el código IR en un programa ejecutable...");
            compilarExe(nombreArchivo);

        } catch (ClassCastException e) {
            // Error sintáctico probablemente
            e.printStackTrace(System.out);
        } catch (Exception e) {
            //e.printStackTrace(System.out);
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
