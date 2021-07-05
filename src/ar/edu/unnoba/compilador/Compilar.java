package ar.edu.unnoba.compilador;

import ar.edu.unnoba.compilador.ast.base.Programa;
import ar.edu.unnoba.compilador.excepciones.ExcepcionVisitor;
import ar.edu.unnoba.compilador.excepciones.GestorExcepciones;
import ar.edu.unnoba.compilador.lexico.Lexer;
import ar.edu.unnoba.compilador.sintaxis.Parser;
import ar.edu.unnoba.compilador.util.Constantes;
import ar.edu.unnoba.compilador.util.Procesos;
import ar.edu.unnoba.compilador.visitor.*;
import ar.edu.unnoba.compilador.visitor.transformer.ConversorDeEstructuras;
import ar.edu.unnoba.compilador.visitor.transformer.Optimizador;
import ar.edu.unnoba.compilador.visitor.transformer.TransformerTipos;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ScannerBuffer;
import java_cup.runtime.Symbol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Clase principal que realiza todos los pasos para convertir código fuente en un ejecutable. */
public class Compilar {
    private static void graficarArbol(Programa programa, String nombreArchivo, String etiqueta) throws IOException, ExcepcionVisitor {
        PrintWriter pw = new PrintWriter(
                new FileWriter(String.format("%s.dot", nombreArchivo)));
        pw.println(new ASTGraphviz(etiqueta).generarCodigo(programa));
        pw.close();

        int exitCode = Procesos.ejecutar("dot", "-T" + Constantes.formatoImgAst,
                nombreArchivo + ".dot",
                "-o", nombreArchivo + "." + Constantes.formatoImgAst);
        if (exitCode == 0) {
            System.out.println("Se graficó el AST «" + nombreArchivo + "».");
        } else {
            System.err.println("Ha ocurrido un error al graficar el AST «" + nombreArchivo + "».");
        }
    }

    private static void generarCodigoIR(Programa p, String archivoEntrada, String archivoSalida)
            throws IOException, ExcepcionVisitor {
        PrintWriter pw = new PrintWriter(new FileWriter(archivoSalida));
        pw.println(new GeneradorDeCodigo().generarCodigo(p, archivoEntrada));
        pw.close();
    }

    private static boolean compilarExe(String nombre) {
        final String nombreExe;
        int exitCode;

        if (GeneradorDeCodigo.targetEsWindows()) {
            nombreExe = nombre + ".exe";

            // Generar objeto con la salida de nuestro compilador
            exitCode = Procesos.ejecutar("clang", "-c", "-o", nombre + ".o", nombre + ".ll");
            if (exitCode == 0) {
                // En Windows no se puede usar la función scanf directamente; generar .exe enlazado a scanf.o
                exitCode = Procesos.ejecutar("clang", "-o", nombreExe, nombre + ".o", "lib/scanf.o");
            }
        } else {
            // Asumimos Unix
            nombreExe = nombre;
            exitCode = Procesos.ejecutar("clang", "-o", nombreExe, nombre + ".ll");
        }

        if (exitCode == 0) {
            System.out.format("Ejecutable generado exitosamente en «%s».\n", nombreExe);
            return true;
        } else {
            System.err.format("Código de salida %d al generar el ejecutable.\n", exitCode);
            return false;
        }
    }

    /** Generar el AST con CUP. Requiere como mínimo CUP 11. */
    private static Programa generarAST(String rutaEntrada) throws Exception {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(rutaEntrada));
        } catch (FileNotFoundException e) {
            System.err.printf("Archivo «%s» no encontrado.%n", rutaEntrada);
            return null;
        }

        ComplexSymbolFactory sf = new ComplexSymbolFactory();
        Lexer lexer = new Lexer(reader, sf);
        Parser parser = new Parser(new ScannerBuffer(lexer), sf);

        Symbol symbol = parser.parse();
        if (symbol == null || !(symbol.value instanceof Programa)) {
            return null;
        }
        return (Programa) symbol.value;
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

        try {
            // Crear directorio de salida si no existe
            Files.createDirectories(Paths.get(carpetaSalida));

            System.out.println("\nGenerando árbol de sintaxis abstracta...");
            Programa programa = generarAST(rutaEntrada);
            // Si programa es nulo es porque hubo un error sintáctico, o no se encontró el archivo.
            // El error ya se tendría que haber mostrado por consola.
            if (programa == null) return;

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
            programa = new TransformerTipos().procesar(programa);

            System.out.println("\nReescribiendo estructuras when y for...");
            programa = new ConversorDeEstructuras().procesar(programa);

            // Mostrar el árbol transformado
            graficarArbol(programa, carpetaSalida + "2_ast-transformado",
                    "AST con conversión de tipos (Conti - Tortoriello)");

            System.out.println("\nIniciando proceso de optimización...");
            programa = new Optimizador().procesar(programa);

            // Mostrar el árbol optimizado
            graficarArbol(programa, carpetaSalida + "3_ast-optimizado",
                    "AST optimizado (Conti - Tortoriello)");

            final String nombreArchivo = "4_llvm-ir";

            System.out.println("\nRecorriendo el programa para detectar cadenas...");
            new ReconocedorCadenas().procesar(programa);

            System.out.println("\nIniciando traducción a código IR...");
            generarCodigoIR(programa, rutaEntrada,  carpetaSalida + nombreArchivo + ".ll");
            System.out.println("\nTraducción a código IR finalizada");

            System.out.println("\nConvirtiendo el código IR en un programa ejecutable...");
            boolean compilado = compilarExe(carpetaSalida + nombreArchivo);
            if (!compilado) return;

            // Para probar, pero desde los IDE primero espera las entradas y después muestra las salidas.
            System.out.println("\nEjecutando el programa compilado...");
            int exitCode = Procesos.ejecutar("./" + carpetaSalida + nombreArchivo);
            if (exitCode != 0) {
                System.err.printf("Código de salida %d al ejecutar el programa.%n", exitCode);
            }
        } catch (Exception e) {
            GestorExcepciones.mostrar(e);
        }
    }
}
