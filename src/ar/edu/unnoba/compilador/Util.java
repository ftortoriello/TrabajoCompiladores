package ar.edu.unnoba.compilador;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Util {

    /* Ejecutar un comando, gestionar su entrada/salida, esperar que finalice y cuando lo haga
     * retornar su código de salida. */
    public static int ejecutar(String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);

        // Usar la I/O del proceso actual
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);

        // Iniciar proceso
        Process proc = pb.start();

        // Esperar que finalice el proceso
        proc.waitFor();
        return proc.exitValue();
    }

}
