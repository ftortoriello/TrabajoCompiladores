#!/bin/sh

"$(dirname "$0")"/run.sh GenerarLexerYParser && \
javac -cp "src:lib/*" -d out -Xlint "src/ar/edu/unnoba/compilador/Compilar.java"

exit $?
