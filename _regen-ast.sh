#!/bin/sh
./_regen-and-test-parser.sh && ./_run.sh GenerarAST
exit $?
