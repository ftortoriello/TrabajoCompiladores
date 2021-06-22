; Programa: Programa
; Muestra 1 en GNU/Linux, y cualquier cosa en Windows si no se pasa como i32 el parámetro de printf.
; Probablemente en Windows quede basura, porque siempre son impares los números que muestra si se pasa 1.
; Igualmente clang siempre extiende i1 a i32 para pasarlo como parámetros.

source_filename = "prueba-print-boolean.txt"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

declare i32 @printf(i8*, ...)

@.int_format_nl = private constant [4 x i8] c"%d\0A\00"

define i32 @main(i32, i8**) {
	%ref.aux.1 = call i32 (i8*, ...) @printf(i8* getelementptr([4 x i8], [4 x i8]* @.int_format_nl, i32 0, i32 0), i1 1)
	ret i32 0
}
