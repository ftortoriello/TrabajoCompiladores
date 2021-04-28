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

// TODO: prioridad mediante paréntesis para operadores (se hace en el parser)

// Comento esto porque las reconozco directamente a partir del string,
// pero lo dejo porque las regex pueden servir para el .cup
// OpAritmeticoSuma= \+|-|\*|\/
// OpComparacion   = ==|\!=|\>|\>=|\<|\<=

// TODO: condiciones
// TODO: sentencias, asignaciones

// TODO: cadenas de caracteres

// TODO: bloques
// TODO: funciones

// TODO: if
// if <condición> then <sentencia>;
// if <condición> then <sentencia1> else <sentencia2>;

// TODO: when
// TODO: while
// TODO: for

Comentario      = #.*{FinDeLinea}            // TODO: Bloques de comentarios

%%

<YYINITIAL> {

    {Comentario}        { /* Ignorar */ }
    {EspacioEnBlanco}   { /* Ignora los espacios en blanco */ }
    "("                 { return token("PAR_ABRE", yytext()); }
    ")"                 { return token("PAR_CIERRA", yytext()); }
    "if"                { return token("PR_IF", yytext()); }
    "then"              { return token("PR_THEN", yytext()); }
    "else"              { return token("PR_ELSE", yytext()); }
    ";"                 { return token("PUNTO_Y_COMA", yytext()); }
    "+"                 { return token("OP_ARIT_SUMA", yytext()); }
    "-"                 { return token("OP_ARIT_RESTA", yytext()); }
    "*"                 { return token("OP_ARIT_PROD", yytext()); }
    "/"                 { return token("OP_ARIT_DIV", yytext()); }
    "=="                { return token("OP_COND_IGUALDAD", yytext()); }
    ">"                 { return token("OP_COND_MAYOR", yytext()); }
    ">="                { return token("OP_COND_MAYOR_IGUAL", yytext()); }
    "<"                 { return token("OP_COND_MENOR", yytext()); }
    "<="                { return token("OP_COND_MENOR_IGUAL", yytext()); }
    "or"                { return token("OP_LOG_BIN_OR", yytext()); }
    "and"               { return token("OP_LOG_BIN_AND", yytext()); }
    "not"               { return token("OP_LOG_UNA_NOT", yytext()); }
    "true"              { return token("VALOR_BOOLEANO_TRUE", yytext()); }
    "false"             { return token("VALOR_BOOLEANO_FALSE", yytext()); }
    "boolean"           { return token("TDD_BOOLEAN", yytext()); }
    "integer"           { return token("TDD_INTEGER", yytext()); }
    "float"             { return token("TDD_FLOAT", yytext()); }
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
