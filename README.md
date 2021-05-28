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

Fecha de entrega: 31/05/2021

![AST](https://i.ibb.co/nmmq5pc/arbol.png)

Requisitos:
* AST, con su visualización mediante graphviz
* Verificación de tipos de datos. Mostrar error si los tipos son incompatibles (por ejemplo: booleanos y números)
  * expresiones
  * variables
  * funciones
  * parámetros
* Opcional: Conversiones implícitas de tipo
* Verificación de ámbito de las variables y funciones

* Admitir variables globales definidas después de la definición de una función. Por ejemplo:
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

#### En general
* Tratar de normalizar los nombres de las variables, por ej. el atributo Identificador a veces es "id", otras "ident" y así.
* Ojo en entrada.txt con la declaración de superficie₁. Por ahora funciona, pero a 0 (la Expresión) lo estamos tomando como INTEGER, y superficie (el Identificador) es FLOAT. La idea de esto es que se solucione cuando implementemos las conversiones.


#### parser.cup
* La *producción* sentencia no es de *tipo* sentencia porque tiene en su interior a invocacion_funcion, que es de tipo expresion. Entonces la producción sentencia está forzada a ser de tipo nodo...
* En la parte de declaración de variables ahora mismo tenemos: dec_var, arg_dec, arg_dec_default, lst_args_dec, lst_args_dec_default, ... tal vez pueda simplificarse.
* Tuve que definir dos precedencias nuevas: para IDENTIFICADOR y para OP_ARIT_RESTA, pero antes no hacían falta. Ver de eliminar la ambigüedad generada al modificar el parser.



## 3.ª Etapa: Generador de código ejecutable a partir del AST

Fecha de entrega: A definirse

Requisitos:
A definirse
