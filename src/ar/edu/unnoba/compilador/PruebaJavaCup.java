package ar.edu.unnoba.compilador;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PruebaJavaCup {

    public static void main(String[] args) {
        try {   
            String path = "src/ar/edu/unnoba/compilador/entrada.txt";
            System.out.println("Análisis sintáctico iniciado:");
            MiLexico lexer = new MiLexico(new FileReader(path));
            MiParser parser = new MiParser(lexer);
            parser.parse();
        } catch (Exception ex) {
            Logger.getLogger(PruebaJavaCup.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
