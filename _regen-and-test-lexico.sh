#!/bin/sh
./_clean.sh && ./_run.sh GenerarLexerYParser && ./_run.sh lexico/PruebaLexer
