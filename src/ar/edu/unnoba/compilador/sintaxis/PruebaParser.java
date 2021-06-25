package ar.edu.unnoba.compilador.sintaxis;

import ar.edu.unnoba.compilador.lexico.Lexer;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PruebaParser {
    public static void main(String[] args) {
        try {
            String path = "entrada.txt";
            System.out.println("Análisis sintáctico iniciado");
            Lexer lexer = new Lexer(new FileReader(path));
            @SuppressWarnings("deprecation") Parser parser = new Parser(lexer);
            parser.parse();
        } catch (Exception ex) {
            Logger.getLogger(PruebaParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
