# Compilador
Trabajo para la asignatura Compiladores de la UNNOBA - 2021

# Guía
El proyecto requiere Java (probado con las versiones 11 y 16), y las bibliotecas [JFlex](https://www.jflex.de/) y [JavaCUP](http://www2.cs.tum.edu/projects/cup/install.php).
En el directorio "lib" se encuentran incluidos los archivos compilados de las versiones más recientes a la fecha de ambas biliotecas.

Los demás archivos del proyecto se encuentran en el paquete "ar.edu.unnoba.compilador" (en el directorio src/ar/edu/unnoba/compilador/).

## Archivos

### lexico.flex

Contiene la implementación en JFlex del analizador léxico (incluyendo expresiones regulares para reconocer tokens, estados del autómata y código Java auxiliar).

### parser.cup

Contiene las reglas para la generación del parser o analizador sintáctico.

### MiToken.java
Clase que representa a cada token identificado por el analizador léxico.

### Generador.java

Al ejecutar esta clase Java generará los siguientes archivos dentro de la misma carpeta:

* MiLexico.java: Archivo con código Java generado por JFlex en base al archivo lexico.flex.
* MiParser.java: Código Java generado por CUP que contiene las reglas semánticas definidas en el archivo parser.cup.
* MiParserSym.java: Similar a MiParser.java, pero conteniendo los símbolos o tokens terminales.

### PruebaJFlex.java

Espera entradas por teclado del usuario, y demuestra cómo el analizador léxico las separa en tokens.

### PruebaJavaCup.java

Lee el texto del archivo **entrada.txt** y muestra las reglas de producción que se aplican al realizar el análisis sintáctico. En el caso de que no se pueda aplicar ninguna regla muestra en qué parte del archivo de entrada se produjo el error de sintaxis.

## Ejecución

### IDE

Se pueden abrir en algún IDE los archivos y configurar el proyecto para incluir las librerías que se encuentran en la carpeta lib.
Se incluyen archivos del proyecto configurado en el IDE "IntelliJ IDEA".

### Línea de comandos

Alternativamente, se pueden compilar y ejecutar los archivos Java mediante la línea de comandos.
Desde el directorio raíz del proyecto:
javac -cp "src:lib/*" -d out -Xlint "src/ar/edu/unnoba/compilador/_ARCHIVO_.java" && \
java -cp "out:lib/*" "ar/edu/unnoba/compilador/_ARCHIVO_"

# Progreso

## 1ra Etapa: Analizador léxico y sintáctico

Fecha de entrega: 04/05/2021 inclusive?

Requisitos obligatorios:

### Análisis léxico
Completo.
Excepto por los casos tipo 10id, que ni idea como solucionar...
Creo que no tienen importancia porque igualmente tendrían que dar error de sintaxis esos casos.

### Comentarios balanceados o Procesamiento de strings con códigos de escape
Completos los dos.

### Análisis sintáctico

#### Una expresión de selección
* if: Completo.
* when: Completo.

#### Una expresión de iteración
* while: Completo.
* for: Completo.

### Otras consideraciones del lenguaje
* Comentarios: Completos los tres tipos.
* Identificadores: Completo.
* Tipos de datos y constantes literales booleanas, numéricas y strings: Completo.
* **Operadores**
  * Revisar la precedencia y asociatividad en operadores aritméticos, comparadores y lógicos.
* Expresiones: Completo.
* Definición de variables: Completo.
* Asignaciones: Completo.
* Funciones:
  * Llamadas: Completo.
  * read_...(), writeln/write(expresion o "cadena"): Completo.
* Declaración de funciones: Completo.
* Estructura general del programa: (declaraciones, main is, cuerpo, end): Completo.

Otros:
* **Renumerar** reglas cuando terminemos.


## 2da Etapa: Analizador semántico con generación del AST

Fecha de entrega: A definirse

Requisitos:
A definirse

## 3ra Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
