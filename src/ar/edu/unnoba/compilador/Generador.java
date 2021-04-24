package ar.edu.unnoba.compilador;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Generador {

    public static void main(String[] args) {
        String path = "src/ar/edu/unnoba/compilador/lexico.flex";
        generarLexer(path);
        
        String[] param = new String[5];
        param[0] = "-destdir";
        param[1] = "src/ar/edu/unnoba/compilador/";
        param[2] = "-parser";
        param[3] = "MiParser";
        param[4] = "src/ar/edu/unnoba/compilador/parser.cup";
        generarParser(param);
    }

    public static void generarLexer(String path){
        File file = new File(path);
        
        //UTILIZAR JFLEX 1.8, la próxima línea no funciona para las versiones previas
        jflex.generator.LexGenerator generator = new jflex.generator.LexGenerator(file);
        generator.generate();
    }
    
    public static void generarParser(String[] param){
        try {
            java_cup.Main.main(param);
        } catch (IOException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
