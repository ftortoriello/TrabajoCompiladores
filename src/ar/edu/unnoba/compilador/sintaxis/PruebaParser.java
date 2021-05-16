package ar.edu.unnoba.compilador.sintaxis;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.edu.unnoba.compilador.lexico.Lexer;

public class PruebaParser {

    public static void main(String[] args) {
        try {   
            String path = "src/ar/edu/unnoba/compilador/entrada.txt";
            System.out.println("Análisis sintáctico iniciado:");
            Lexer lexer = new Lexer(new FileReader(path));
            Parser parser = new Parser(lexer);
            parser.parse();
        } catch (Exception ex) {
            Logger.getLogger(PruebaParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
