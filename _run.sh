#!/bin/sh

SRCDIR="ar/edu/unnoba/compilador"
if [ ! -f "src/${SRCDIR}/$1.java" ] ; then
  echo "Uso: $0 archivo_java"
  echo "  Por ejemplo: Generador, EjemploJFlex, EjemploJavaCup"
  exit 1
fi

javac -cp "src:lib/*" -d out -Xlint "src/ar/edu/unnoba/compilador/$1.java" && \
java -cp "out:lib/*" "ar/edu/unnoba/compilador/$1"

exit $?
