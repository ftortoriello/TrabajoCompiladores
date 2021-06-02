package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.visitor.ASTGraphviz;
import ar.edu.unnoba.compilador.visitor.GeneradorAlcances;
import ar.edu.unnoba.compilador.visitor.TransformerTipos;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GenerarAST {
    public static void main(String[] args) throws Exception {
        String ruta = System.getProperty("user.dir") + "/src/ar/edu/unnoba/compilador/entrada.txt";
        FileReader entrada = new FileReader(ruta);
        Lexer lexico = new Lexer(entrada);
        @SuppressWarnings("deprecation") Parser parser = new Parser(lexico);
        try {
            Programa programa = (Programa) parser.parse().value;

            // Ejecución de visitor graficador
            PrintWriter pw = new PrintWriter(new FileWriter("arbol.dot"));
            ASTGraphviz graficador = new ASTGraphviz();
            pw.println(graficador.visit(programa));
            pw.close();
            String cmd = "dot -Tpng arbol.dot -o arbol.png";
            Runtime.getRuntime().exec(cmd);

            // Ejecución de visitor generador de alcances
            GeneradorAlcances ga = new GeneradorAlcances();
            ga.procesar(programa);
            System.out.println("Alcances procesados");

            // Ejecución de transformer validador de tipos
            TransformerTipos vt = new TransformerTipos();
            vt.procesar(programa);
            System.out.println("Tipos validados");
        } catch (ClassCastException e) {
            // Error sintáctico
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
