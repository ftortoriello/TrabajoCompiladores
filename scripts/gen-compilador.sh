#!/bin/sh

cd "$(dirname "$0")/.."

scripts/run.sh GenerarLexerYParser && \
javac -cp "src:lib/*" -d out -Xlint "src/ar/edu/unnoba/compilador/Compilar.java"

exit $?
