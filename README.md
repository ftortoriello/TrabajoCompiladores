# Compilador
Trabajo para la asignatura "Compiladores" - UNNOBA - 2021
[https://github.com/ftortoriello/TrabajoCompiladores](https://github.com/ftortoriello/TrabajoCompiladores)

# Progreso

## 1ra Etapa: Analizador léxico y sintáctico

Fecha de entrega: 04/05/2021

Requisitos obligatorios:

### Análisis léxico
Estado: :white_check_mark:, menos casos raros (00, 10variable, etc.) que tratamos en la parte sintáctica.

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
* Operadores: :white_check_mark:
* Expresiones: :white_check_mark:
* Definición de variables: :white_check_mark:
* Asignaciones: :white_check_mark:
* Funciones:
  * Llamadas: :white_check_mark:
  * read_...(), writeln/write(expresion o "cadena"): :white_check_mark:
* Declaración de funciones: :white_check_mark:
* Estructura general del programa: (declaraciones, main is, cuerpo, end): :white_check_mark:

## 2da Etapa: Analizador semántico con generación del AST

Fecha de entrega: 31/05/2021

Requisitos:
* AST, con su visualización mediante graphviz
* Verificación de tipos de datos. Mostrar error si los tipos son incompatibles (por ejemplo: booleanos y números)
  * expresiones
  * variables
  * funciones
  * parámetros
* Opcional: Conversiones implícitas de tipo
* Verificación de ámbito de las variables y funciones

* Adminir variables globales definidas despues de la definicion de una funcion. Por ejemplo:
```
function mayor(x is integer)
    x = aux;
    return x;

int aux = 10;

main is
    mayor(10);
end.
```

### Revisar

#### parser.cup
...

## 3ra Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
