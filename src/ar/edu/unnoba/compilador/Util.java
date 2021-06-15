package ar.edu.unnoba.compilador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {
    // Ejecutar un comando, mostrar su salida y cuando finalice retornar su código de salida
    public static int ejecutar(String[] cmd) throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec(cmd);

        String line;
        BufferedReader reader;

        // stdout
        reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // stderr
        boolean error = false;
        reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            if (!error) {
                // mostrar título
                System.out.println("ERRORES:");
                error = true;
            }
            System.out.println(line);
        }
        proc.waitFor(); // esperar que finalice el proceso
        return proc.exitValue();
    }
}
