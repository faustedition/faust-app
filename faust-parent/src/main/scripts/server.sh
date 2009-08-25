#!/bin/bash

##
## find base dir
##
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done

BASE="`dirname \"$PRG\"`"
BASE_DIR=`dirname "$BASE"`
BASE_NAME=`basename "$BASE"`
BASE="`cd \"$BASE_DIR\" 2>/dev/null && pwd || echo \"$BASE_DIR\"`/$BASE_NAME"

cd $BASE

WEBAPP=`echo $BASE/webapps/faust-webapp*`
exec java -Xmx1024m\
	-Dlog4j.configuration=file://$BASE/log4j.properties\
	-Dfaust.webapp=$WEBAPP\
	-Dwicket.configuration=deployment\
	-jar $BASE/lib/faust-server*.jar
