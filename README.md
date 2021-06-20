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
* Transformación de estruras when y for:  :white_check_mark: 
* Constant folding: :white_check_mark:

Más que opcional:
* Reemplazo de constantes por su valor.
* Eliminación de asignaciones redundantes (ej: x = x).
* Eliminación de las declaraciones de variables sin uso.
* Código inaccesible.

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
  * Read: :x:
    * Truncamiento o conversiones: :x:
  * Write: :x:
* Declaración de variables (locales y globales, inicializadas y no inicializadas): :white_check_mark:
  * Valores por defecto para las no inicializadas: :white_check_mark:
* Bloques de código:
  * Asignaciones: :white_check_mark:
  * Operaciones lógicas: :x:
    * Cortocircuito: :x:
  * Operaciones relacionales: :white_check_mark:
  * Operaciones aritméticas: :white_check_mark:
  * Invocación de funciones definidas en el lenguaje: :white_check_mark:
  * Literales: :white_check_mark:
  * If: :white_check_mark:
  * While: :white_check_mark:
    * Sentencias de control (continue y break): :x:

Cosas secundarias:
* Estaría bueno ver si podemos indentar el código IR producido. Pero tiene que ser una forma genérica sino es una locura, por ej. cuando hacemos el return del visit del bloque. Estuve viendo y hay una función `indent` que lo hace prácticamente solo (hasta tiene en cuenta los saltos de línea), el tema es que la estructura de bloques que tenemos no es muy consistente y a veces indenta y a veces no. Pero por lo menos indentar 1 nivel (porque no sé si habría más) todo lo que está dentro de las funciones.
  Ahora estoy poniendo tabulaciones "a mano" en todo lo que no es global, una etiqueta o una declaración o cierre de función. Pensé y empecé a hacer indentar por niveles de bloque del código de entrada (con un contador de nivel, también es manual), pero no creo que quede bien, porque en el IR no van a existir esos niveles.
* Eliminar clase "Para" y "Cuando". Generar la transformación de esas estructuras en el propio parser (posiblemente creando un constructor nuevo en las clases Mientras y SiEntonces, cosa de tirarlo ahí y resolverlo en la propia clase).
* Ver de crear métodos para instrucciones comunes del IR (store, load, etc.), cosa de que queden todas centralizadas en un lugar y no andar creando los strings por todos lados. Por ej. podríamos tener imprimirAlloca(nombre, tipo), y que se encargue esa función del string cosa de que el resto sea más fácil de leer.
