package ar.edu.unnoba.compilador.lexico;

import java.io.IOException;
import java.io.InputStreamReader;
import java_cup.runtime.Symbol;

public class PruebaLexer {

    public static void main(String[] args) throws IOException {
        Lexer lexico = new Lexer(new InputStreamReader(System.in));
        System.out.printf("Análisis léxico iniciado: %nIngrese por teclado:%n");
        while (true) {
            try {
                Symbol token = lexico.next_token();
                if (token == null)
                    break;
                System.out.println("Token: " + token.toString());
            } catch (java.lang.Error e) {
                System.out.println(e);
            }
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