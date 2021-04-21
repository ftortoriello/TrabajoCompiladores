/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejemplo.jflex;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author itt
 */
public class Generador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String path = "./src/ejemplo/jflex/lexico.flex";
        generarLexer(path);
        
        String[] param = new String[5];
        param[0] = "-destdir";
        param[1] = "./src/ejemplo/jflex/";
        param[2] = "-parser";
        param[3] = "MiParser";
        param[4] = "./src/ejemplo/jflex/parser.cup";
        generarParser(param);
    }
    
    /**
     *
     * @param path
     */
    public static void generarLexer(String path){
        File file=new File(path);
        
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
