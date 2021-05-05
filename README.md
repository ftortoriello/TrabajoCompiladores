# Compilador
Trabajo para la asignatura "Compiladores" - UNNOBA - 2021

# Progreso

## 1ra Etapa: Analizador léxico y sintáctico

Fecha de entrega: 04/05/2021 inclusive

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

## Revisar
### parser.cup
* precedence right PR_ELSE;: parece que no cambia nada si se pone left acá... Igual funciona como debería
* precedence left PR_IS;: está bien así?


## 2da Etapa: Analizador semántico con generación del AST

Fecha de entrega: A definirse

Requisitos:
A definirse

## 3ra Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
