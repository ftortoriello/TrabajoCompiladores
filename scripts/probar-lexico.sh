#!/bin/sh

run="$(dirname "$0")/run.sh"

"$run" GenerarLexerYParser && \
"$run" lexico/PruebaLexer

exit $?
