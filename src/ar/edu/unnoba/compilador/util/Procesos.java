package ar.edu.unnoba.compilador.util;

import java.io.IOException;

public class Procesos {
    /**
     * Ejecutar un comando, gestionar su entrada/salida, esperar que finalice, y cuando lo haga
     * retornar su código de salida.
     */
    public static int ejecutar(String... cmd) {
        if (cmd.length <= 0) {
            System.err.println("ejecutar(): No se pasó un comando.");
            return -1;
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);

        // Usar la I/O del proceso actual
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);

        // Iniciar proceso
        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            System.err.println("No se pudo ejecutar el comando: " + cmd[0]);
            return -1;
        }

        // Esperar que finalice el proceso
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Se interrumpió la ejecución del programa.");
        }
        return proc.exitValue();
    }
}
