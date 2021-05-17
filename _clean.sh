#!/bin/sh
find . -type f \( \
	-name '*.class' -o -name '*~' \
	-o -name 'Lexer.java' \
	-o -name 'Parser.java' -o -name 'ParserSym.java' \
	\) -delete -print
rm -rfv bin build dist out
rm -fv .classpath .project
