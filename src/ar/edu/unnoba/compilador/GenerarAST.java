package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.visitor.*;

import java.io.*;

public class GenerarAST {
    private static void graficarArbol(String dot, String nombreArchivo) throws IOException {
        final String formatoImg = "png";
        //final String formatoImg = "svg";
        PrintWriter pw = new PrintWriter(
                new FileWriter(String.format("%s.dot", nombreArchivo)));
        pw.println(dot);
        pw.close();
        Runtime.getRuntime().exec(
                String.format("dot -T%2$s %1$s.dot -o %1$s.%2$s",
                        nombreArchivo, formatoImg));
        System.out.println("\nSe graficó el AST «" + nombreArchivo + "».");
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

        Lexer lexico = new Lexer(entrada);
        @SuppressWarnings("deprecation") Parser parser = new Parser(lexico);
        try {
            Programa programa = (Programa) parser.parse().value;
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
        } catch (ClassCastException e) {
            // Error sintáctico
            e.printStackTrace(System.out);
        } catch (Exception e) {
            //e.printStackTrace(System.out);
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
