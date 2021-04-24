package ar.edu.unnoba.compilador;

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

FinDeLinea      =  \r|\n|\r\n
EspacioEnBlanco =  \s


DigitoSinCero   =   [1-9]
Digito          =   [0-9]
Entero          =   0|{DigitoSinCero}{Digito}* // TODO: modificar para aceptar decimales

Id = ([:letter:]|_)\w*      // TODO: modificar según el enunciado

TipoDeDato = boolean|integer|float
// TODO: agregar constantes booleanas

// TODO: ver cómo aceptar comentarios anidados
%%
   
<YYINITIAL> {

    {EspacioEnBlanco}   { /* Ignora los espacios en blanco */ }
    "+"                 { return token("MAS", yytext()); }
    "-"                 { return token("MENOS", yytext()); }
    "/"                 { return token("DIV", yytext()); }
    "*"                 { return token("MULT", yytext()); }
    "("                 { return token("PAR_ABRE", yytext()); }  
    ")"                 { return token("PAR_CIERRA", yytext()); }
    {TipoDeDato}        { return token("TIPO_DE_DATO", yytext()); }
    {Id}                { return token("IDENTIFICADOR", yytext()); }
    {Entero}            { return token("ENTERO", yytext()); }

}
/* probarrrr:: 10variable
   Toma 10 como ENTERO y el resto como IDENTIFICADOR sin importar el orden de las reglas (así que está mal) */

[^]                     { throw new Error("Entrada no permitida: <" + yytext() + ">"); }
