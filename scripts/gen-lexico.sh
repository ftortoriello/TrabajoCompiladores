#!/bin/sh
"$(dirname "$0")"/run.sh GenerarLexerYParser && \
"$(dirname "$0")"/run.sh lexico/PruebaLexer
exit $?
