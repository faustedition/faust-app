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
WEBAPP=`echo $BASE/webapps/faust-webapp*`
exec java -Dfaust.webapp=$WEBAPP -jar $BASE/lib/faust-server*.jar
