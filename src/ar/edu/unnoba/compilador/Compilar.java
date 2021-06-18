package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.ast.base.excepciones.ExcepcionDeAlcance;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.visitor.*;
import ar.edu.unnoba.compilador.visitor.transformer.ConversorDeEstructuras;
import ar.edu.unnoba.compilador.visitor.transformer.Optimizador;
import ar.edu.unnoba.compilador.visitor.transformer.TransformerTipos;
import java_cup.runtime.Symbol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/* Clase principal que realiza todos los pasos para convertir código fuente en un ejecutable */

public class Compilar {
    private static void graficarArbol(Programa programa, String nombreArchivo, String etiqueta) throws IOException, InterruptedException, ExcepcionDeAlcance {
        final String formatoImg = "png";
        //final String formatoImg = "svg";

        PrintWriter pw = new PrintWriter(
                new FileWriter(String.format("%s.dot", nombreArchivo)));
        pw.println(new ASTGraphviz(etiqueta).generarCodigo(programa));
        pw.close();

        int exitCode = Util.ejecutar(new String[] {
                "dot", "-T" + formatoImg,
                nombreArchivo + ".dot",
                "-o", nombreArchivo + "." + formatoImg
        });
        if (exitCode == 0) {
            System.out.println("Se graficó el AST «" + nombreArchivo + "».");
        } else {
            System.out.println("Ha ocurrido un error al graficar el AST «" + nombreArchivo + "».");
        }
    }

    private static void generarCodigoIR(Programa p, String archivoEntrada, String archivoSalida)
            throws IOException, ExcepcionDeAlcance {
        PrintWriter pw = new PrintWriter(new FileWriter(archivoSalida));
        pw.println(new GeneradorDeCodigo().generarCodigo(p, archivoEntrada));
        pw.close();
    }

    private static void compilarExe(String nombre) throws IOException, InterruptedException {
        final String nombreExe;
        int exitCode;

        if (GeneradorDeCodigo.targetEsWindows()) {
            nombreExe = nombre + ".exe";

            // en Windows hay que enlazarlo con scanf.o para permitir leer entradas del teclado
            // TODO: una vez que implementemos las funciones read, probar si esto es necesario para Windows y no los demás

            // generar objeto con la salida de nuestro compilador
            exitCode = Util.ejecutar(new String[] {
                    "clang", "-c", "-o", nombre + ".o", nombre + ".ll"
            });
            if (exitCode == 0) {
                // generar exe enlazado al objeto anterior y a scanf
                exitCode = Util.ejecutar(new String[]{
                        "clang", "-o", nombreExe, nombre + ".o", "scanf.o"
                });
            }
        } else {
            // asumimos Unix
            nombreExe = nombre;
            exitCode = Util.ejecutar(new String[] {
                    "clang", "-o", nombreExe, nombre + ".ll"
            });
        }

        if (exitCode == 0) {
            System.out.format("Ejecutable generado exitosamente en «%s».\n", nombreExe);
        } else {
            System.out.format("Código de salida %d al generar el ejecutable.\n", exitCode);
        }
    }

    public static void main(String[] args) {
        final String rutaEntrada;
        final String carpetaSalida = "salida/";

        if (args.length > 0) {
            // Tomar ruta de archivo fuente de la línea de comandos
            rutaEntrada = args[0];
        } else {
            // Ruta predeterminada
            rutaEntrada = "entrada.txt";
        }

        FileReader entrada;
        try {
            entrada = new FileReader(rutaEntrada);
        } catch (FileNotFoundException e) {
            System.out.println("No se pudo encontrar el archivo: " + rutaEntrada);
            return;
        }

        Lexer lexico = new Lexer(entrada);
        @SuppressWarnings("deprecation") Parser parser = new Parser(lexico);
        try {
            // Crear directorio de salida si no existe
            Files.createDirectories(Paths.get(carpetaSalida));

            Symbol simboloPrograma = parser.parse();
            if (simboloPrograma == null) {
                System.out.println("\nEntrada vacía.");
                return;
            }
            if (!(simboloPrograma.value instanceof Programa)) {
                // Error sintáctico, ya se tendría que haber mostrado el error
                return;
            }

            Programa programa = (Programa) simboloPrograma.value;

            // Ejecutar Visitor graficador del árbol sin transformar,
            // y convertirlo a imagen
            graficarArbol(programa, carpetaSalida + "1_ast-original",
                    "Árbol de sintaxis abstracta (Conti - Tortoriello)");

            System.out.println("\nIniciando generador de alcances globales...");
            new GeneradorDeAlcanceGlobal().procesar(programa);

            System.out.println("\nIniciando generador de alcances locales...");
            new GeneradorDeAlcancesLocales().procesar(programa);

            System.out.println("\nIniciando validador de sentencias de control...");
            new VisitorControl().procesar(programa);

            System.out.println("\nIniciando validación y conversión de tipos...");
            new TransformerTipos().procesar(programa);

            System.out.println("\nReescribiendo estructuras when y for...");
            new ConversorDeEstructuras().procesar(programa);

            // Mostrar el árbol transformado
            graficarArbol(programa, carpetaSalida + "2_ast-transformado",
                    "AST con conversión de tipos (Conti - Tortoriello)");

            System.out.println("\nIniciando proceso de optimización...");
            new Optimizador().procesar(programa);

            // Mostrar el árbol optimizado
            graficarArbol(programa, carpetaSalida + "3_ast-optimizado",
                    "AST optimizado (Conti - Tortoriello)");

            final String nombreArchivo = "4_entrada";

            System.out.println("\nIniciando traducción a código IR...");
            generarCodigoIR(programa, rutaEntrada,  carpetaSalida + nombreArchivo + ".ll");
            System.out.println("\nTraducción a código IR finalizada");

            System.out.println("\nConvirtiendo el código IR en un programa ejecutable...");
            compilarExe(carpetaSalida + nombreArchivo);

        } catch (ClassCastException e) {
            // Error sintáctico probablemente
            e.printStackTrace(System.out);
        } catch (Exception e) {
            //e.printStackTrace(System.out);
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
