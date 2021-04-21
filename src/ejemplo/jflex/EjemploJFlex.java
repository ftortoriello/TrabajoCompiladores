/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejemplo.jflex;

import java.io.IOException;
import java.io.InputStreamReader;
import java_cup.runtime.Symbol;

/**
 *
 * @author Merce
 */
public class EjemploJFlex {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    
    public static void main(String[] args) throws IOException {
        
        // TODO code application logic here
        MiLexico lexico = new MiLexico(new InputStreamReader(System.in));
        System.out.printf("Análisis léxico iniciado: %nIngrese por teclado:%n");
        while (true) {
            Symbol token = lexico.next_token();
            if (token == null)
                break;
            System.out.println("Token: " + token.toString());
        }
        lexico.tablaDeSimbolos.stream().map((simbolo) -> {
            System.out.println(simbolo.nombre);
            return simbolo;
        }).forEachOrdered((simbolo) -> {
            System.out.println(simbolo.valor);
        });
        System.out.println("Análisis léxico terminado.");

    }

    
}
