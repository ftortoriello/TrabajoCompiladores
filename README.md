# TrabajoCompiladores
Trabajo final para la asignatura Compiladores de la UNNOBA

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
* if: Casi está (faltarían los bloques de sentencias y ver bien cuando van los punto y coma)
* when: TODO

#### Una expresión de iteración
* while: ídem if
* for: TODO

### Otras consideraciones del lenguaje
* Comentarios: Completos los tres tipos.
* Identificadores: Completo.
* Tipos de datos y constantes literales booleanas, numéricas y strings: Completo.
* Operadores:
  * Falta la resta unaria.
  * Agregar paréntesis.
  * Revisar la precedencia y asociatividad en operadores aritméticos, comparadores y lógicos.
* Expresiones: Completo?
* Definición de variables: Las globales están, falta agregar las locales una vez que estén los bloques begin/end.
* Asignaciones: Completo.
* Funciones:
  * Llamadas: Completo.
  * read_...(), writeln/write(expresion o "cadena"): Completo.
* Declaración de funciones: Falta.
* Estructura general del programa: (declaraciones, main is, cuerpo, end): Completo.


## 2da Etapa: Analizador semántico con generación del AST

Fecha de entrega: A definirse

Requisitos:
A definirse

## 3ra Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
