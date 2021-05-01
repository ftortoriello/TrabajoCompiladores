#!/bin/sh

SRCDIR="ar/edu/unnoba/compilador"
if [ ! -f "src/${SRCDIR}/$1.java" ] ; then
  echo "Uso: $0 archivo_java"
  echo "  Por ejemplo: Generador, EjemploJFlex, EjemploJavaCup"
  exit 1
fi

pushd src > /dev/null
javac -cp ".:../lib/*" -Xlint "ar/edu/unnoba/compilador/$1.java" && \
java -cp ".:../lib/*" "ar/edu/unnoba/compilador/$1" && \
popd > /dev/null

exit $?
