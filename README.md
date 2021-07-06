# Compilador
Trabajo para la asignatura "Compiladores" - UNNOBA - 2021
[https://github.com/ftortoriello/TrabajoCompiladores](https://github.com/ftortoriello/TrabajoCompiladores)

## Ejemplo del lenguaje

![codigo](https://user-images.githubusercontent.com/82975077/123492931-508a2f00-d5f1-11eb-82f7-41897bdb654e.png)
![ast](https://user-images.githubusercontent.com/82975077/123492687-97c3f000-d5f0-11eb-8473-957206aaa1de.png)

# Progreso

## 1.ª Etapa: Analizador léxico y sintáctico

Fecha de entrega: 04/05/2021

Requisitos obligatorios:

### Análisis léxico
Estado: :white_check_mark:, menos casos raros (00, 10variable, etc.) que tratamos en la parte sintáctica.

### Comentarios balanceados o Procesamiento de strings con códigos de escape
Estado: :white_check_mark:

### Análisis sintáctico
Estado: :white_check_mark:

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

## 2.ª Etapa: Analizador semántico con generación del AST

Fecha de entrega: 04/06/2021

Requisitos:
* AST, con su visualización mediante graphviz: :white_check_mark:
* Verificación de tipos de datos. Mostrar error si los tipos son incompatibles (por ejemplo: booleanos y números): :white_check_mark:
  * expresiones
  * variables
  * funciones
  * parámetros
* Verificación de ámbito de las variables y funciones: :white_check_mark:

Opcional: 
* Conversiones implícitas de tipo: :white_check_mark:
* Transformación de estruras when y for: :white_check_mark:
* Constant folding: :white_check_mark:

Más que opcional:
* Reemplazo de constantes por su valor.
* Eliminación de las declaraciones de variables sin uso.
* Eliminación de asignaciones redundantes (ej: x = x).

### Revisar

#### parser.cup
* La *producción* sentencia no es de *tipo* sentencia porque tiene en su interior a invocacion_funcion, que es de tipo expresion. Entonces la producción sentencia está forzada a ser de tipo nodo...

## 3.ª Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: 25/06/2021

Requisitos:
* Estructura gral. del programa: :white_check_mark:
* Declaración de funciones: :white_check_mark:
  * Parámetros opcionales: :white_check_mark:
  * Valor de retorno por defecto (si no hay return): :white_check_mark:
* Funciones predefinidas o externas:
  * Read: :white_check_mark:
  * Write: :white_check_mark:
  * Truncamiento o conversiones: :white_check_mark:
* Declaración de variables (locales y globales, inicializadas y no inicializadas): :white_check_mark:
  * Valores por defecto para las no inicializadas: :white_check_mark:
* Bloques de código:
  * Asignaciones: :white_check_mark:
  * Operaciones lógicas: :white_check_mark:
    * Cortocircuito: :white_check_mark:
  * Operaciones relacionales: :white_check_mark:
  * Operaciones aritméticas: :white_check_mark:
  * Invocación de funciones definidas en el lenguaje: :white_check_mark:
  * Literales: :white_check_mark:
  * If: :white_check_mark:
  * While: :white_check_mark:
    * Sentencias de control (continue y break): :white_check_mark:

Mejoras en caso de que tengamos que seguir:
  * Manejar caso a and NOT b en el cortocircuito booleano.
  * Añadir referencia a DecFun en Invocacion para poder eliminar las tablas de funciones.
  * Implementar visit(SimboloVariable) para no tener que convertir constantemente desde Identificador a SimboloVariable en los últimos visitors.
