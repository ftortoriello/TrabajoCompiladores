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
    private final StringBuffer stringBuffer = new StringBuffer();

    private int cadena_yyline = 0;
    private int cadena_yycolumn = 0;

    private enum TipoComentario { LLAVES, PASCAL }
    private final Stack<TipoComentario> comentariosAbiertos = new Stack<>();

    public Lexer(java.io.Reader in, ComplexSymbolFactory sf) {
		this(in);
        symbolFactory = sf;
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
        throw new Error("Línea " + this.yyline + ", columna " + this.yycolumn + ": " + msg);
    }
%}

EspacioEnBlanco     = \s

// \p{L}: letras Unicode - \p{N}: números Unicode
Id                  = [\p{L}_][\p{L}\p{N}_]*\??

// 10id ni 0.123.4 tendrían que ser aceptado como dos tokens distintos.
// Podría solucionarse agregando \b a las regex, pero no funciona en JFlex :(
// Igualmente en esos casos tiene que fallar el análisis sintáctico.
Entero              = \d+
Flotante            = \d*\.\d*

OpComparacion       = ==|\!=|\>|\>=|\<|\<=
TiposDeDato         = boolean|integer|float
CtesBooleanas       = true|false

FinDeLinea         = \r|\n|\r\n
ComentarioUnaLinea = #.*{FinDeLinea}?

%state BLOQUE_COMENT CADENA

%%

<YYINITIAL> {
    \"                  { stringBuffer.setLength(0); yybegin(CADENA);
                          /* guardar posición inicial para dejarla bien en el lexema */
                          cadena_yyline = this.yyline; cadena_yycolumn = this.yycolumn; }
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); yybegin(BLOQUE_COMENT); }
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); yybegin(BLOQUE_COMENT); }
    (\}|\*\))           { errorLexico("Cierre de comentario inesperado"); }
    {ComentarioUnaLinea} { /* ignorar */ }

    // Palabras reservadas
    "main is"           { return symbol("PR_MAIN_IS", yytext()); }
    "end."              { return symbol("PR_END_PUNTO", yytext()); }
    "variable"          { return symbol("PR_VARIABLE", yytext()); }
    "is"                { return symbol("PR_IS", yytext()); }
    "function"          { return symbol("PR_FUNCTION", yytext()); }
    "return"            { return symbol("PR_RETURN", yytext()); }
    "begin"             { return symbol("PR_BEGIN", yytext()); }
    "end"               { return symbol("PR_END", yytext()); }
    "if"                { return symbol("PR_IF", yytext()); }
    "then"              { return symbol("PR_THEN", yytext()); }
    "else"              { return symbol("PR_ELSE", yytext()); }
    "when"              { return symbol("PR_WHEN", yytext()); }
    "while"             { return symbol("PR_WHILE", yytext()); }
    "for"               { return symbol("PR_FOR", yytext()); }
    "from"              { return symbol("PR_FROM", yytext()); }
    "to"                { return symbol("PR_TO", yytext()); }
    "by"                { return symbol("PR_BY", yytext()); }
    "do"                { return symbol("PR_DO", yytext()); }
    "break"             { return symbol("PR_BREAK", yytext()); }
    "continue"          { return symbol("PR_CONTINUE", yytext()); }
    "writeln"           { return symbol("PR_WRITELN", yytext()); }
    "write"             { return symbol("PR_WRITE", yytext()); }
    "read_integer"      { return symbol("PR_READ_INTEGER", yytext()); }
    "read_float"        { return symbol("PR_READ_FLOAT", yytext()); }
    "read_boolean"      { return symbol("PR_READ_BOOLEAN", yytext()); }
    {TiposDeDato}       { return symbol("TIPO_DE_DATO", yytext()); }

    // Operadores
    "+"                 { return symbol("OP_ARIT_SUMA", yytext()); }
    "-"                 { return symbol("OP_ARIT_RESTA", yytext()); }
    "*"                 { return symbol("OP_ARIT_PROD", yytext()); }
    "/"                 { return symbol("OP_ARIT_DIV", yytext()); }

    {OpComparacion}     { return symbol("OP_COMPARACION", yytext()); }

    "or"                { return symbol("OP_LOG_BIN_OR", yytext()); }
    "and"               { return symbol("OP_LOG_BIN_AND", yytext()); }
    "not"               { return symbol("OP_LOG_UNA_NOT", yytext()); }

    // Otros símbolos del lenguaje
    "("                 { return symbol("PAR_ABRE", yytext()); }
    ")"                 { return symbol("PAR_CIERRA", yytext()); }

    ","                 { return symbol("COMA", yytext()); }
    ";"                 { return symbol("PUNTO_Y_COMA", yytext()); }
    "="                 { return symbol("IGUAL", yytext()); }

    {EspacioEnBlanco}   { /* ignorar */ }

    // Literales
    {CtesBooleanas}     { return symbol("LIT_BOOLEANO", yytext()); }
    {Flotante}          { return symbol("LIT_FLOTANTE", yytext()); }
    {Entero}            { return symbol("LIT_ENTERO", yytext()); }

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
                            return symbol("CADENA", cadena_yyline, cadena_yycolumn, stringBuffer.toString());
                        }
    \\\"                { stringBuffer.append('"'); }   // comilla doble escapada: \"
    \\t                 { stringBuffer.append('\t'); }  // tabulación
    \\n                 { stringBuffer.append('\n'); }  // salto de línea
    \\r                 { stringBuffer.append('\r'); }  // retorno de carro
    \\\\                { stringBuffer.append('\\'); }  // \\ --> \
    /* permitir lo demás, excepto \ que no estén seguidas por lo definido arriba */
    [^\\]               { stringBuffer.append( yytext() ); }
}

<<EOF>> { return null; }

[^]                     { errorLexico("Entrada no permitida: (" + yytext() + ")"); }
