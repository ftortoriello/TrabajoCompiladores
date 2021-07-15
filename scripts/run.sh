#!/bin/sh

cd "$(dirname "$0")/.."

if [ ! -f "src/ar/edu/unnoba/compilador/$1.java" ] ; then
	echo "Uso: $0 archivo_java"
	echo "  Por ejemplo: GenerarLexerYParser, PruebaLexer, PruebaParser, Compilar"
	exit 1
fi

file="$1"
shift

javac -cp "src:lib/*" -d out -Xlint "src/ar/edu/unnoba/compilador/$file.java" && \
java -cp "out:lib/*" "ar/edu/unnoba/compilador/$file" "$@"

exit $?
