package ar.edu.unnoba.compilador.ast.expresiones.valor;

import ar.edu.unnoba.compilador.ast.expresiones.Expresion;
import ar.edu.unnoba.compilador.visitor.Visitor;
import ar.edu.unnoba.compilador.visitor.transformer.Transformer;

import java.nio.charset.StandardCharsets;

public class Cadena extends Expresion {

    private String valor;
    private String nombreIR;

    public Cadena(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getNombreIR() {
        return nombreIR;
    }

    public void setNombreIR(String nombreIR) {
        this.nombreIR = nombreIR;
    }

    /* Obtener cadena con sus caracteres no ASCII reemplazados por el código UTF-8
     * (por ej: á --> \C3\A1), y con el carácter final nulo. */
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

    /* Retorna la cantidad de bytes, incluyendo el nulo. */
    public int getLongitudIR() {
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

    @Override
    public String toString() {
        return getValor();
    }
}
