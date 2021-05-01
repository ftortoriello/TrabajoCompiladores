package ar.edu.unnoba.compilador;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java_cup.runtime.*;
import java_cup.sym;

/* Implementación del analizador léxico para el TP Integrador de Compiladores
 * Alumnos:
 * Bruno Conti
 * Franco Tortoriello
 * Facundo Oyola
 */

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
    
    int string_yyline = 0;
    int string_yycolumn = 0;

    StringBuffer string = new StringBuffer();

    enum TipoComentario { LLAVES, PASCAL }
    Stack<TipoComentario> comentariosAbiertos = new Stack<TipoComentario>();

    public List<MiToken> tablaDeSimbolos = new ArrayList<>();

    /* sirven para algo los tokens con valor null??? */
    /*
    private MiToken token(String nombre) {
        return new MiToken(nombre, this.yyline, this.yycolumn);
    }
    */

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

FinDeLinea      = \r|\n|\r\n
EspacioEnBlanco = \s

DigitoSinCero   =  [1-9]
Digito          =  \d /*[0-9]*/
/* Los Identificadores tendrían que soportar números y letras como superscript o subscript... Ej: x²
 * https://en.wikipedia.org/wiki/Unicode_subscripts_and_superscripts#Superscripts_and_subscripts_block */
DigitoScript    = [\u2070\u00B9\u00B2\u00B3\u2074-\u2079\u2080-\u2089]
LetraScript     = [\u2071\u207F\u2090-\u209C]
Id              = ([:letter:]|_|{LetraScript})[\w{LetraScript}{DigitoScript}]*\??
Entero          = 0|{DigitoSinCero}{Digito}*  /* FIXME: pasa similar a 10variable, toma 001 como 0 y 01... */
Flotante        = ({Entero}\.{Digito}*|\.{Digito}*)


OpAritSumaYResta    = \+|-
OpAritProdYDiv      = \*|\/
OpComparacion       = ==|\!=|\>|\>=|\<|\<=
TiposDeDato         = boolean|integer|float
CtesBooleanas       = true|false

// TODO: cadenas de caracteres
// TODO: palabras reservadas para when, while, for y otros

ComentarioUnaLinea  = #.*{FinDeLinea}            // TODO: Bloques de comentarios

%state COMENT_LLAVES COMENT_PASCAL STRING

%%

<YYINITIAL> {
    \"                  { string.setLength(0); yybegin(STRING);
                          string_yyline = this.yyline; string_yycolumn = this.yycolumn; }
    "main is"           { return token("PR_MAIN_IS", yytext()); }
    "end."              { return token("PR_END_PUNTO", yytext()); }
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); yybegin(COMENT_LLAVES); }
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); yybegin(COMENT_PASCAL); }
    (\}|\*\))           { errorLexico("Cierre de comentario inesperado"); }
    {ComentarioUnaLinea} { /* ignorar */ }
    {EspacioEnBlanco}   { /* ignorar */ }
    {OpAritSumaYResta}  { return token("OP_ARIT_SUMA_O_RESTA", yytext()); }
    {OpAritProdYDiv}    { return token("OP_ARIT_PROD_O_DIV", yytext()); }
    {OpComparacion}     { return token("OP_COMPARACION", yytext()); }
    {CtesBooleanas}     { return token("CTE_BOOLEANA", yytext()); }
    {TiposDeDato}       { return token("TIPO_DE_DATO", yytext()); }
    "("                 { return token("PAR_ABRE", yytext()); }
    ")"                 { return token("PAR_CIERRA", yytext()); }
    "variable"          { return token("PR_VARIABLE", yytext()); }
    "is"                { return token("PR_IS", yytext()); }
    "function"          { return token("PR_FUNCTION", yytext()); }
    "return"            { return token("PR_RETURN", yytext()); }
    ","                 { return token("PR_COMA", yytext()); }
    "begin"             { return token("PR_BEGIN", yytext()); }
    "end"               { return token("PR_END", yytext()); }
    "if"                { return token("PR_IF", yytext()); }
    "then"              { return token("PR_THEN", yytext()); }
    "else"              { return token("PR_ELSE", yytext()); }
    ";"                 { return token("PUNTO_Y_COMA", yytext()); }
    "or"                { return token("OP_LOG_BIN_OR", yytext()); }
    "and"               { return token("OP_LOG_BIN_AND", yytext()); }
    "not"               { return token("OP_LOG_UNA_NOT", yytext()); }
    "="                 { return token("IGUAL", yytext()); }
    "write"             { return token("PR_WRITE", yytext()); }
    "writeln"           { return token("PR_WRITELN", yytext()); }
    "read_"             { return token("PR_READ_GB", yytext()); }
    {Id}                { return token("IDENTIFICADOR", yytext()); }
    {Flotante}          { return token("FLOTANTE", yytext()); }
    {Entero}            { return token("ENTERO", yytext()); }

}

<COMENT_LLAVES> {
    "}"                 {
                            if (comentariosAbiertos.pop() == TipoComentario.LLAVES) {
                                /* se cerro un comentario de llaves */
                                if (comentariosAbiertos.empty()) {
                                    /* se cerraron todos los comentarios */
                                    yybegin(YYINITIAL);
                                } else if (comentariosAbiertos.peek() == TipoComentario.PASCAL) {
                                    yybegin(COMENT_PASCAL);
                                }
                            } else {
                                errorLexico("Error del programador al cerrar un bloque de comentario de llaves. Esto no debería ocurrir...");
                            }
                        }

                        /* comentarios anidados */
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); }    /* seguir en este estado */
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); yybegin(COMENT_PASCAL); }

    "*)"                { errorLexico("Se esperaba un cierre de comentario de llaves, no de tipo Pascal"); }

    [^]                 { /* ignorar todo lo demás */ }
}

<COMENT_PASCAL> {
    "*)"                {
                            if (comentariosAbiertos.pop() == TipoComentario.PASCAL) {
                                /* se cerro un comentario tipo Pascal */
                                if (comentariosAbiertos.empty()) {
                                    /* se cerraron todos los comentarios */
                                    yybegin(YYINITIAL);
                                } else if (comentariosAbiertos.peek() == TipoComentario.LLAVES) {
                                    yybegin(COMENT_PASCAL);
                                }
                            } else {
                                errorLexico("Error del programador al cerrar un bloque de comentario de tipo Pascal. Esto no debería ocurrir...");
                            }
                        }

                        /* comentarios anidados */
    "(*"                { comentariosAbiertos.push(TipoComentario.PASCAL); }    /* seguir en este estado */
    "{"                 { comentariosAbiertos.push(TipoComentario.LLAVES); yybegin(COMENT_LLAVES); }

    "}"                 { errorLexico("Se esperaba un cierre de comentario de tipo Pascal, no de llaves"); }

    [^]                 { /* ignorar todo lo demás */ }
}

<STRING> {
    \"                  {   /* fin del string */
                            yybegin(YYINITIAL);
                            return token("STRING", string_yyline, string_yycolumn, string.toString());
                        }
    {FinDeLinea}        { string.append('\n'); }
    [^]                 { string.append( yytext() ); }
    /* TODO ver si hace falta \t, \n, etc */
}

/* probarrrr:: 10variable
   Toma 10 como ENTERO y el resto como IDENTIFICADOR sin importar el orden de las reglas (así que está mal)
   Está bien que no lo tome como IDENTIFICADOR porque tiene que comenzar con una letra o _, pero tendría que
   tirar un error sintáctico creo...... tampoco es 10 * variable porque tiene que estar el "*" (y calculo que
   los espacios) "*/

[^]                     { errorLexico("Entrada no permitida: '" + yytext() + "'"); }

