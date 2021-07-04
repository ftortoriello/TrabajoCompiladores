package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.nio.charset.StandardCharsets;

public class Cadena extends Expresion {
    private String valor;   // siempre tiene comillas escapadas (\")
    private String ptroIR;

    public Cadena(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getPtroIR() {
        return ptroIR;
    }

    public void setPtroIR(String ptroIR) {
        this.ptroIR = ptroIR;
    }

    /** Obtener cadena sin las comillas externas */
    private String getValorSinComillas() {
        return valor.substring(2, valor.length() - 2);
    }

    /**
     * Obtener representación con los caracteres de escape escapados.
     * Usado en los archivos DOT para que el gráfico las muestre idénticas al código de entrada
     * y no quede con errores de sintaxis.
     */
    @Override
    public String toString() {
        // Agregar escape en los caracteres de escape.
        // Tomar la cadena sin comillas externas para que no se reemplacen las barras.
        return "\\\"" + getValorSinComillas()
                .replace("\\", "\\\\\\\\")
                .replace("\t", "\\\\t")
                .replace("\n", "\\\\n")
                .replace("\r", "\\\\r")
                .replace("\"", "\\\\\\\"")
                + "\\\"";
    }

    /**
     * Obtener cadena con sus caracteres no ASCII reemplazados por el código UTF-8
     * (por ej: á --> \C3\A1), y con el carácter final nulo.
     */
    public String getValorIR() {
        byte[] utf8bytes = getValorSinComillas().getBytes(StandardCharsets.UTF_8);
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
        // Longitud sin las comillas (-2*2) y con el fin nulo (+1)
        return valor.getBytes(StandardCharsets.UTF_8).length - 3;
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
