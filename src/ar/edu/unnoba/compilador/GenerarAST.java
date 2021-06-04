package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.visitor.ASTGraphviz;
import ar.edu.unnoba.compilador.visitor.GeneradorAlcances;
import ar.edu.unnoba.compilador.visitor.TransformerTipos;

import java.io.*;

public class GenerarAST {
    private static void graficarArbol(String dot, String nombreArchivo) throws IOException {
        final String formatoImg = "png";
        PrintWriter pw = new PrintWriter(
                new FileWriter(String.format("%s.dot", nombreArchivo)));
        pw.println(dot);
        pw.close();
        Runtime.getRuntime().exec(
                String.format("dot -T%2$s %1$s.dot -o %1$s.%2$s",
                        nombreArchivo, formatoImg));
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

            // Ejecutar Visitor graficador del árbol sin transformar,
            // y convertirlo a imagen
            graficarArbol(new ASTGraphviz().visit(programa),"arbol-orig");

            // Ejecuctar Visitor generador de alcances
            GeneradorAlcances ga = new GeneradorAlcances();
            ga.visit(programa);
            System.out.println("Alcances procesados");

            // Ejecutar Transformer validador de tipos
            TransformerTipos tt = new TransformerTipos();
            tt.transform(programa);
            System.out.println("Tipos validados");

            // Mostrar el árbol transformado
            graficarArbol(new ASTGraphviz().visit(programa),"arbol-transformado");
        } catch (ClassCastException e) {
            // Error sintáctico
        } catch (Exception e) {
            //e.printStackTrace(System.out);
            System.out.println(String.format("%s: %s", e.getClass().getSimpleName(), e.getLocalizedMessage()));
        }
    }
}
