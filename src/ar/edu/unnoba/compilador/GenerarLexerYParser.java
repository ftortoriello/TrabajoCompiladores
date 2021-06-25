package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.excepciones.GestorExcepciones;

import java.io.File;

/**
 * Clase para generar las clases Java del analizador léxico y sintáctico.
 * Se debe ejecutar antes de generar el compilador.
 */
public class GenerarLexerYParser {
    public static void main(String[] args) {
        generarLexer("src/ar/edu/unnoba/compilador/lexico/lexer.jflex");

        generarParser("-destdir", "src/ar/edu/unnoba/compilador/sintaxis",
                "-parser", "Parser",
                "-locations",     // permitir guardar la posición en la entrada de cada símbolo
                "-dump_grammar",  // mostrar la gramática creada
                "src/ar/edu/unnoba/compilador/sintaxis/parser.cup");
    }

    public static void generarLexer(String ruta) {
        File file = new File(ruta);

        // Esto requiere como mínimo JFlex 1.8
        jflex.generator.LexGenerator generator = new jflex.generator.LexGenerator(file);
        generator.generate();
    }

    public static void generarParser(String... params) {
        try {
            java_cup.Main.main(params);
        } catch (Exception e) {
            GestorExcepciones.mostrar(e);
        }
    }
}
