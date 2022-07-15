#!/bin/sh
# -------------------------------------------------------------------------
# MHDSEND  Launcher
# -------------------------------------------------------------------------

MAIN_CLASS=kr.irm.fhir.MHDReferenceSearch

DIRNAME="`dirname "$0"`"

# Setup $MHDREFERENCESEARCH_HOME
if [ "x$MHDREFERENCESEARCH_HOME" = "x" ]; then
    MHDREFERENCESEARCH_HOME=`cd "$DIRNAME"/..; pwd`
fi

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA="java"
fi

# Setup the classpath
CP="$MHDREFERENCESEARCH_HOME/etc/MHDReferenceSearch/"
for s in $MHDREFERENCESEARCH_HOME/lib/*.jar
do
	CP="$CP:$s"
done

# Execute the JVM

exec $JAVA $JAVA_OPTS -cp "$CP" $MAIN_CLASS "$@"