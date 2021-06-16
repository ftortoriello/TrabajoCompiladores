#!/bin/sh

find . -type f \( \
	-name '*.class' -o -name '*~' \
	-o -name 'Lexer.java' \
	-o -name 'Parser.java' -o -name 'ParserSym.java' \
	\) -delete -print
rm -rfv bin build dist out salida
rm -fv .classpath .project

exit 0
