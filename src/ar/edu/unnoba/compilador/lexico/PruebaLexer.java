package ar.edu.unnoba.compilador.lexico;

import ar.edu.unnoba.compilador.excepciones.GestorExcepciones;
import ar.edu.unnoba.compilador.sintaxis.Util;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;

import java.io.IOException;
import java.io.InputStreamReader;

public class PruebaLexer {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(new InputStreamReader(System.in), new ComplexSymbolFactory());
        System.out.println("Análisis léxico iniciado.\nIngrese caracteres por teclado:");
        while (true) {
            try {
                ComplexSymbol token = (ComplexSymbol) lexer.next_token();
                if (token == null)
                    break;
                System.out.printf("Token: %s (%s)%n", token.getName(),
                                  Util.getPosicion(token.getLeft(), token.getRight()));
            } catch (java.lang.Error e) {
                GestorExcepciones.mostrar(e);
            }
        }
        lexer.tablaDeSimbolos.stream().peek((simbolo) ->
                System.out.println(simbolo.value)
        ).forEachOrdered((simbolo) ->
                System.out.println(simbolo.value)
        );
        System.out.println("Análisis léxico terminado.");
    }
}
