package ar.edu.unnoba.compilador.ast.expresiones;

import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Cadena extends Expresion {
    private String valor;
    private String ptroIR;

    /**
     * Valor de la cadena entre comillas dobles y con caracteres especiales escapados.
     * Usado en los archivos DOT para que no queden con errores de sintaxis.
     */
    private String valorEsc;

    public Cadena(String valor) {
        setValor(valor);
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
        setValorEsc();
    }

    public String getPtroIR() {
        return ptroIR;
    }

    public void setPtroIR(String ptroIR) {
        this.ptroIR = ptroIR;
    }

    @Override
    public String toString() {
        return valorEsc;
    }

    private void setValorEsc() {
        final String barraInv = "\\";
        Map<Character, String> reemp = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        // Reemplazos
        reemp.put('\t', barraInv.repeat(2) + "t");
        reemp.put('\n', barraInv.repeat(2) + "n");
        reemp.put('\r', barraInv.repeat(2) + "r");
        reemp.put('"', barraInv.repeat(3) + '"');
        reemp.put('\\', barraInv.repeat(4));

        // Agregar comillas
        sb.append(barraInv).append('"');

        // Agregar cada carácter de la cadena original, reemplazándolo si es necesario
        for (char c : valor.toCharArray()) {
            String s = reemp.get(c);
            sb.append(s == null ? c : s);
        }

        sb.append(barraInv).append('"');

        valorEsc = sb.toString();
    }

    /**
     * Obtener cadena con sus caracteres no ASCII reemplazados por el código UTF-8
     * (por ej: á --> \C3\A1), y con el carácter final nulo.
     */
    public String getValorIR() {
        byte[] utf8bytes = valor.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : utf8bytes) {
            if (b >= 0x20 && b <= 0x7E && b != 0x22) {
                // Es ASCII imprimible y no es comilla doble; dejarlo legible
                byte[] byteASCII = { b };
                sb.append(new String(byteASCII));
            } else {
                // Convertirlo a código
                int code = Byte.toUnsignedInt(b);
                String hex = Integer.toHexString(code).toUpperCase();
                sb.append("\\");
                // Tiene que tener dos dígitos
                if (code < 16) sb.append("0");
                sb.append(hex);
            }
        }
        // Agregar fin nulo
        sb.append("\\00");
        return sb.toString();
    }

    /** Obtener la cantidad de bytes, incluyendo el nulo. */
    public int getLongitudIR() {
        // Cantidad de bytes + fin nulo
        return valor.getBytes(StandardCharsets.UTF_8).length + 1;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public Cadena accept(Transformer t) {
        return this;
    }

    public Expresion evaluar() {
        return this;
    }
}
