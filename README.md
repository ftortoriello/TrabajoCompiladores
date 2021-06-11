# Compilador
Trabajo para la asignatura "Compiladores" - UNNOBA - 2021
[https://github.com/ftortoriello/TrabajoCompiladores](https://github.com/ftortoriello/TrabajoCompiladores)

# Progreso

## 1.ª Etapa: Analizador léxico y sintáctico

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

## 2.ª Etapa: Analizador semántico con generación del AST

Fecha de entrega: 04/06/2021

![AST](https://user-images.githubusercontent.com/82975077/120876361-ae919c80-c586-11eb-88c6-0c91690013c7.png)


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
* Transformación de estruras when y for:  :white_check_mark:, pero falta ver cómo solucionamos el tema de las nuevas variables (¿volver a pasar el visitor generador de alcances?)
* Constant folding: :x:. casos básicos funcionando (por ej. 1 + 2), los más complejos no (por ej. 1 + x + 2 tendría que quedar x + 3 o 3 + x)
* Reemplazo de constantes por su valor: :x:
* Eliminación de las declaraciones de variables sin uso: :x:

### Revisar

#### parser.cup
* La *producción* sentencia no es de *tipo* sentencia porque tiene en su interior a invocacion_funcion, que es de tipo expresion. Entonces la producción sentencia está forzada a ser de tipo nodo...
* En la parte de declaración de variables ahora mismo tenemos: dec_var, arg_dec, arg_dec_default, lst_args_dec, lst_args_dec_default, ... tal vez pueda simplificarse.
* Tuve que definir dos precedencias nuevas: para IDENTIFICADOR y para OP_ARIT_RESTA, pero antes no hacían falta. Ver de eliminar la ambigüedad generada al modificar el parser.


## 3.ª Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: 25/06/2021

Requisitos:
* Estructura gral. del programa: :x:
* Declaración de funciones: :x:
* Funciones predefinidas: :x:
* Declaración de variables: :x:
* Asignaciones: :x:
* Operaciones lógicas: :x:
* Operaciones relacionales: :x:
* Operaciones aritméticas: :x:
* Estructuras de selección: :x:
* Estructuras de iteración: :x:
* Invocación de funciones: :x:

Cosas secundarias:
* Tomar la configuración de la máquina anfitrión de un archivo que no esté en el git para no pisarnos.
* Ver excepciones en GeneradorDeCodigo.java. Está usando ExcepcionDeAlcance porque el visitor abstracto tiene esas definidad en la firma de los métodos. Probé cambiarlas por Exception para que quede más abstracto y poder definir excepciones específicas en cada visitor, pero igual hay lío porque esos visitors llaman a su padre. Y todos los nodos tienen también definido un tipo específico de excepción en los métodos accept y visit.