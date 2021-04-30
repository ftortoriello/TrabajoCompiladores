package ar.edu.unnoba.compilador;

import java.util.List;
import java.util.ArrayList;
import java_cup.runtime.*;
import java_cup.sym;

%%

%public
/* nombre de la clase generada */
%class MiLexico
/* nombre de la clase para representar tokens (objetos retornados por yylex) */
/* %type Nombre */
%unicode
%cup
/* permitir conocer el número de línea que se está analizando mediante this.yyline */
%line
/* ídem con el número de columna */
%column
/* ídem con el número de carácter */
/* %char */

%{
    /*************************************************************************
    * Código a copiar textualmente como parte de la definición de la clase
    *  del analizador léxico.
    *************************************************************************/
    
    public List<MiToken> tablaDeSimbolos = new ArrayList<>();
 
	/* Preguntar para que sin valor ... */
    private MiToken token(String nombre) {
        return new MiToken(nombre, this.yyline, this.yycolumn);
    }

    private MiToken token(String nombre, Object valor) {
        return new MiToken(nombre, this.yyline, this.yycolumn, valor);
    }
%}

FinDeLinea      = \r|\n|\r\n
EspacioEnBlanco = \s

DigitoSinCero   =  [1-9]
Digito          =  [0-9]
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

Comentario      = #.*{FinDeLinea}            // TODO: Bloques de comentarios

%%

<YYINITIAL> {
    "main is"           { return token("PR_MAIN_IS", yytext()); }
    "end."              { return token("PR_END_PUNTO", yytext()); }
    {Comentario}        { /* Ignorar */ }
    {EspacioEnBlanco}   { /* Ignora los espacios en blanco */ }
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
    {Id}                { return token("IDENTIFICADOR", yytext()); }
    {Flotante}          { return token("FLOTANTE", yytext()); }
    {Entero}            { return token("ENTERO", yytext()); }

}

/* probarrrr:: 10variable
   Toma 10 como ENTERO y el resto como IDENTIFICADOR sin importar el orden de las reglas (así que está mal)
   Está bien que no lo tome como IDENTIFICADOR porque tiene que comenzar con una letra o _, pero tendría que
   tirar un error sintáctico creo...... tampoco es 10 * variable porque tiene que estar el "*" (y calculo que
   los espacios) "*/

[^]                     { throw new Error("Entrada no permitida: <" + yytext() + ">"); }
