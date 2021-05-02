package ar.edu.unnoba.compilador;

import java.io.IOException;
import java.io.InputStreamReader;
import java_cup.runtime.Symbol;

public class PruebaJFlex {

    public static void main(String[] args) throws IOException {
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
