# Compilador
Trabajo para la asignatura "Compiladores" - UNNOBA - 2021

# Progreso

## 1ra Etapa: Analizador léxico y sintáctico

Fecha de entrega: 04/05/2021

Requisitos obligatorios:

### Análisis léxico

### Comentarios balanceados o Procesamiento de strings con códigos de escape
Estado: :white_check_mark:	

### Análisis sintáctico
Estado: :white_check_mark:, pero hay cuestiones pendientes con los operadores (ver más abajo).

#### Una expresión de selección
* if: :white_check_mark:	
* when: :white_check_mark:	

#### Una expresión de iteración
* while: :white_check_mark:	
* for: :white_check_mark:	

### Otras consideraciones del lenguaje
* Comentarios: :white_check_mark:	
* Identificadores: :white_check_mark:	
* Tipos de datos y constantes literales booleanas, numéricas y strings: :white_check_mark:	
* **Operadores** :exclamation:
  * Revisar la precedencia y asociatividad en operadores aritméticos, comparadores y lógicos.
* Expresiones: :white_check_mark:	
* Definición de variables: :white_check_mark:	
* Asignaciones: :white_check_mark:	
* Funciones:
  * Llamadas: :white_check_mark:	
  * read_...(), writeln/write(expresion o "cadena"): :white_check_mark:	
* Declaración de funciones: :white_check_mark:	
* Estructura general del programa: (declaraciones, main is, cuerpo, end): :white_check_mark:	

## Revisar
### parser.cup
* precedence right PR_ELSE;: parece que no cambia nada si se pone left acá... Igual funciona como debería
* precedence left PR_IS;: está bien así?
* ¿Por qué los números pasan por la regla de operaciones lógicas? Ver si se puede definir más precisamente. Ejemplo de reglas en una asignación:
```
REGLA 11.1.2: FLOTANTE (1.5) -> numero_constante
REGLA 11.1: numero_constante (1.5) -> factor
REGLA 8.2: factor (1.5) -> expresion_logica_basica
REGLA 7.2: expresion_logica_basica (1.5) -> expresion_logica_not
REGLA 6.2: expresion_logica_not (1.5) -> expresion_logica_and
REGLA 5.2: expresion_logica_and (1.5) -> expresion_logica_or
REGLA 4.1: expresion_logica_or (1.5) -> operacion
```

## 2da Etapa: Analizador semántico con generación del AST

Fecha de entrega: A definirse

Requisitos:
A definirse

## 3ra Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
