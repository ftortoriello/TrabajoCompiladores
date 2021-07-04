package ar.edu.unnoba.compilador.lexico;

import ar.edu.unnoba.compilador.sintaxis.ParserSym;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

%%

// Visibilidad de la clase generada
%public
// Nombre de la clase generada
%class Lexer
// Manejar entradas con caracteres Unicode
%unicode
// Permitir conocer el número de línea que se está analizando mediante this.yyline
%line
// Idem con el número de columna
%column
// Hacer la clase generada compatible con CUP
%cup

%{
    /*************************************************************************
    * Código a copiar textualmente como parte de la definición de la clase
    * del analizador léxico.
    *************************************************************************/

    ComplexSymbolFactory symbolFactory;

    public final List<Symbol> tablaDeSimbolos = new ArrayList<>();
    private final StringBuilder stringBuilder = new StringBuilder();

    private int cadena_yyline = 0;
    private int cadena_yycolumn = 0;

    private enum TipoComentario { LLAVES, PASCAL }
    private final Stack<TipoComentario> comentariosAbiertos = new Stack<>();

    public Lexer(java.io.Reader in, ComplexSymbolFactory sf) {
		this(in);
        symbolFactory = sf;
    }

    private Symbol symbol(String nombre) {
        return symbol(nombre, yyline + 1, yycolumn + 1, null);
    }

    private Symbol symbol(String nombre, Object valor) {
        return symbol(nombre, yyline + 1, yycolumn + 1, valor);
    }

    private Symbol symbol(String nombre, int linea, int columna, Object valor) {
        return symbolFactory.newSymbol(nombre, getSymbolId(nombre),
                new Location(linea, columna), new Location(linea, columna + yylength() - 1),
                 valor);
    }

    private int getSymbolId(String nombre) {
        return Arrays.asList(ParserSym.terminalNames).indexOf(nombre);
    }

    /** Mostrar error léxico, incluyendo la posición del scanner */
    private void errorLexico(String msg) {
        throw new Error(String.format("Línea:%d Columna:%d - %s", yyline+1, yycolumn+1, msg));
    }
%}

EspacioEnBlanco     = \s

// \p{L}: letras Unicode - \p{N}: números Unicode
Id                  = [\p{L}_][\p{L}\p{N}_]*\??

// 10id ni 0.123.4 tendrían que ser aceptado como dos tokens distintos.
// Podría solucionarse agregando \b a las regex, pero no funciona en JFlex :(
// Igualmente en esos casos tiene que fallar el análisis sintáctico.
Entero              = \d+
Flotante            = \d+\.|\.\d+|\d+\.\d+

OpComparacion       = ==|\!=|\>|\>=|\<|\<=
TiposDeDato         = boolean|integer|float
CtesBooleanas       = true|false

FinDeLinea         = \r|\n|\r\n
ComentarioUnaLinea = #.*{FinDeLinea}?

%state BLOQUE_COMENT CADENA

%%

<YYINITIAL> {
    \"                  { stringBuilder.setLength(0); yybegin(CADENA);
                          /* guardar posición inicial para dejarla bien en el lexema */
                          cadena_yyline = this.yyline; cadena_yycolumn = this.yycolumn; }
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); yybegin(BLOQUE_COMENT); }
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); yybegin(BLOQUE_COMENT); }
    (\}|\*\))           { errorLexico("Cierre de comentario inesperado"); }
    {ComentarioUnaLinea} { /* ignorar */ }

    // Palabras reservadas
    "main is"           { return symbol("PR_MAIN_IS"); }
    "end."              { return symbol("PR_END_PUNTO"); }
    "variable"          { return symbol("PR_VARIABLE"); }
    "is"                { return symbol("PR_IS"); }
    "function"          { return symbol("PR_FUNCTION"); }
    "return"            { return symbol("PR_RETURN"); }
    "begin"             { return symbol("PR_BEGIN"); }
    "end"               { return symbol("PR_END"); }
    "if"                { return symbol("PR_IF"); }
    "then"              { return symbol("PR_THEN"); }
    "else"              { return symbol("PR_ELSE"); }
    "when"              { return symbol("PR_WHEN"); }
    "while"             { return symbol("PR_WHILE"); }
    "for"               { return symbol("PR_FOR"); }
    "from"              { return symbol("PR_FROM"); }
    "to"                { return symbol("PR_TO"); }
    "by"                { return symbol("PR_BY"); }
    "do"                { return symbol("PR_DO"); }
    "break"             { return symbol("PR_BREAK"); }
    "continue"          { return symbol("PR_CONTINUE"); }
    "writeln"           { return symbol("PR_WRITELN"); }
    "write"             { return symbol("PR_WRITE"); }
    "read_integer"      { return symbol("PR_READ_INTEGER"); }
    "read_float"        { return symbol("PR_READ_FLOAT"); }
    "read_boolean"      { return symbol("PR_READ_BOOLEAN"); }
    {TiposDeDato}       { return symbol("TIPO_DE_DATO", yytext()); }

    // Operadores
    "+"                 { return symbol("OP_ARIT_SUMA"); }
    "-"                 { return symbol("OP_ARIT_RESTA"); }
    "*"                 { return symbol("OP_ARIT_PROD"); }
    "/"                 { return symbol("OP_ARIT_DIV"); }

    {OpComparacion}     { return symbol("OP_COMPARACION", yytext()); }

    "or"                { return symbol("OP_LOG_BIN_OR"); }
    "and"               { return symbol("OP_LOG_BIN_AND"); }
    "not"               { return symbol("OP_LOG_UNA_NOT"); }

    // Otros símbolos del lenguaje
    "("                 { return symbol("PAR_ABRE"); }
    ")"                 { return symbol("PAR_CIERRA"); }

    ","                 { return symbol("COMA"); }
    ";"                 { return symbol("PUNTO_Y_COMA"); }
    "="                 { return symbol("IGUAL"); }

    {EspacioEnBlanco}   { /* ignorar */ }

    // Literales
    {CtesBooleanas}     { return symbol("LIT_BOOLEANO", Boolean.parseBoolean(yytext())); }
    {Flotante}          { return symbol("LIT_FLOTANTE", Double.parseDouble(yytext())); }
    {Entero}            { return symbol("LIT_ENTERO", Integer.parseInt(yytext())); }

    {Id}                { return symbol("IDENTIFICADOR", yytext()); }
}

<BLOQUE_COMENT> {
                        /* comentarios anidados */
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); }
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); }

    "}"                 {
                            if (comentariosAbiertos.pop() == TipoComentario.LLAVES) {
                                if (comentariosAbiertos.empty()) {
                                    yybegin(YYINITIAL);
                                }
                            } else {
                                errorLexico("Cierre inesperado de bloque de comentario de llaves");
                            }
                        }

    "*)"                {
                            if (comentariosAbiertos.pop() == TipoComentario.PASCAL) {
                                if (comentariosAbiertos.empty()) {
                                    yybegin(YYINITIAL);
                                }
                            } else {
                                errorLexico("Cierre inesperado de bloque de comentario de tipo Pascal");
                            }
                        }

    [^]                 { /* ignorar lo demás */ }
}

<CADENA> {
    \"                  {   // Fin de la cadena
                            yybegin(YYINITIAL);
                            return symbol("CADENA", cadena_yyline, cadena_yycolumn, stringBuilder.toString());
                        }
    \\\"                { stringBuilder.append('"'); }   // comilla doble escapada: \"
    \\t                 { stringBuilder.append('\t'); }  // tabulación
    \\n                 { stringBuilder.append('\n'); }  // salto de línea
    \\r                 { stringBuilder.append('\r'); }  // retorno de carro
    \\\\                { stringBuilder.append('\\'); }  // \\ --> \
    /* permitir lo demás, excepto \ que no estén seguidas por lo definido arriba */
    [^\\]               { stringBuilder.append( yytext() ); }
}

<<EOF>> { return null; }

[^]                     { errorLexico("Entrada no permitida: (" + yytext() + ")"); }
