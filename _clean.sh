#!/bin/sh
find . -type f \( \
	-name '*.class' -o -name '*~' \
	-o -name 'MiLexico.java' -o -name 'MiParser.java' \
	-o -name 'MiParserSym.java' \
	\) -delete -print
rm -rfv bin build dist out
rm -fv .classpath .project
