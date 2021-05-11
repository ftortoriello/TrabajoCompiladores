package ar.edu.unnoba.compilador;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java_cup.runtime.*;
import java_cup.sym;

/* Implementación del analizador léxico */

%%

/* visibilidad de la clase generada */
%public
/* nombre de la clase generada */
%class MiLexico
/* nombre de la clase para representar tokens (objetos retornados por yylex) */
//%type MiToken
/* finalizar la lectura cuando se encuentra EOF */
//%eofclose
/* soportar entradas con caracteres Unicode */
%unicode
/* permitir conocer el número de línea que se está analizando mediante this.yyline */
%line
/* ídem con el número de columna */
%column
/* ídem con el número de carácter */
/* %char */
/* utilizar CUP */
%cup

%{
    /*************************************************************************
    * Código a copiar textualmente como parte de la definición de la clase
    *  del analizador léxico.
    *************************************************************************/
    
    int cadena_yyline = 0;
    int cadena_yycolumn = 0;

    StringBuffer stringBuffer = new StringBuffer();

    enum TipoComentario { LLAVES, PASCAL }
    Stack<TipoComentario> comentariosAbiertos = new Stack<TipoComentario>();

    public List<MiToken> tablaDeSimbolos = new ArrayList<>();

    private MiToken token(String nombre) {
        return new MiToken(nombre, this.yyline, this.yycolumn);
    }

    private MiToken token(String nombre, Object valor) {
        return new MiToken(nombre, this.yyline, this.yycolumn, valor);
    }

    private MiToken token(String nombre, int line, int column, Object valor) {
        return new MiToken(nombre, line, column, valor);
    }

    /* unificar formato de mensajes de error */
    private void errorLexico(String msg) {
        throw new Error("Línea " + this.yyline + ", columna " + this.yycolumn + ": " + msg);
    }
%}

EspacioEnBlanco     = \s

// \p{L}: letras Unicode - \p{N}: números Unicode
Id                  = [\p{L}_][\p{L}\p{N}_]*\??

/* 10id ni 0.123.4 tendrían que ser aceptado como dos tokens distintos.
 * Podría solucionarse agregando \b a las regex, pero no funciona en JFlex :(
 * Igualmente en esos casos tiene que fallar el análisis sintáctico.
 */
Entero              = \d+
Flotante            = \d*\.\d*

OpAritSuma          = \+
OpAritResta         = -
OpAritProdYDiv      = \*|\/
OpComparacion       = ==|\!=|\>|\>=|\<|\<=
TiposDeDato         = boolean|integer|float
CtesBooleanas       = true|false

FinDeLinea         = \r|\n|\r\n
ComentarioUnaLinea = #.*{FinDeLinea}

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

    /* palabras reservadas */
    "main is"           { return token("PR_MAIN_IS", yytext()); }
    "end."              { return token("PR_END_PUNTO", yytext()); }
    "variable"          { return token("PR_VARIABLE", yytext()); }
    "is"                { return token("PR_IS", yytext()); }
    "function"          { return token("PR_FUNCTION", yytext()); }
    "return"            { return token("PR_RETURN", yytext()); }
    "begin"             { return token("PR_BEGIN", yytext()); }
    "end"               { return token("PR_END", yytext()); }
    "if"                { return token("PR_IF", yytext()); }
    "then"              { return token("PR_THEN", yytext()); }
    "else"              { return token("PR_ELSE", yytext()); }
    "when"              { return token("PR_WHEN", yytext()); }
    "while"             { return token("PR_WHILE", yytext()); }
    "for"               { return token("PR_FOR", yytext()); }
    "from"              { return token("PR_FROM", yytext()); }
    "to"                { return token("PR_TO", yytext()); }
    "by"                { return token("PR_BY", yytext()); }
    "do"                { return token("PR_DO", yytext()); }
    "break"             { return token("PR_BREAK", yytext()); }
    "continue"          { return token("PR_CONTINUE", yytext()); }
    "writeln"           { return token("PR_WRITELN", yytext()); }
    "write"             { return token("PR_WRITE", yytext()); }
    "read_integer"      { return token("PR_READ_INTEGER", yytext()); }
    "read_float"        { return token("PR_READ_FLOAT", yytext()); }
    "read_boolean"      { return token("PR_READ_BOOLEAN", yytext()); }
    {TiposDeDato}       { return token("TIPO_DE_DATO", yytext()); }
    {CtesBooleanas}     { return token("CTE_BOOLEANA", yytext()); }
  
    /* operadores */
    {OpAritSuma}        { return token("OP_ARIT_SUMA", yytext()); }
    {OpAritResta}       { return token("OP_ARIT_RESTA", yytext()); }
    {OpAritProdYDiv}    { return token("OP_ARIT_PROD_O_DIV", yytext()); }

    {OpComparacion}     { return token("OP_COMPARACION", yytext()); }
    "or"                { return token("OP_LOG_BIN_OR", yytext()); }
    "and"               { return token("OP_LOG_BIN_AND", yytext()); }
    "not"               { return token("OP_LOG_UNA_NOT", yytext()); }
 
    "("                 { return token("PAR_ABRE", yytext()); }
    ")"                 { return token("PAR_CIERRA", yytext()); }

    ","                 { return token("PR_COMA", yytext()); }
    ";"                 { return token("PUNTO_Y_COMA", yytext()); }
    "="                 { return token("IGUAL", yytext()); }

    {EspacioEnBlanco}   { /* ignorar */ }
    {Id}                { return token("IDENTIFICADOR", yytext()); }
    {Flotante}          { return token("FLOTANTE", yytext()); }
    {Entero}            { return token("ENTERO", yytext()); }
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

    [^]                 { /* ignorar todo lo demás */ }
}

<CADENA> {
    \"                  {   /* fin de la cadena */
                            yybegin(YYINITIAL);
                            return token("CADENA", cadena_yyline, cadena_yycolumn, stringBuffer.toString());
                        }
    \\\"                { stringBuffer.append('"'); }   // comillas dobles escapadas (\")
    \\t                 { stringBuffer.append('\t'); }
    \\n                 { stringBuffer.append('\n'); }
    \\r                 { stringBuffer.append('\r'); }
    \\\\                { stringBuffer.append('\\'); }  // \\ --> \
    /* permitir todo lo demás, excepto \ que no estén seguidas por lo definido arriba */
    [^\\]               { stringBuffer.append( yytext() ); }
}

[^]                     { errorLexico("Entrada no permitida: (" + yytext() + ")"); }

