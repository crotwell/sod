#!/bin/sh
MAVEN=$HOME/repository

#if using cygwin, use windows friendly options
cygwin=false;
case "`uname`" in
	CYGWIN*) cygwin=true;;
esac
SEP=:
if $cygwin ; then
	SEP='\;'
	MAVEN=`cygpath -w $MAVEN`
fi

# for orbacus
OB=$MAVEN/OB/jars/OB-4.1.0.jar
OBNAMING=$MAVEN/OBNaming/jars/OBNaming-4.1.0.jar

SEEDCODEC=$MAVEN/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/fissuresUtil/jars/fissuresUtil-1.0Beta.jar
FISSURESIMPL=$MAVEN/fissuresImpl/jars/fissuresImpl-1.1Beta.jar
FISSURESIDL=$MAVEN/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/log4j/jars/log4j-1.2.6.jar
TAUP=$MAVEN/TauP/jars/TauP-1.1.4.jar
XERCES=$MAVEN/xerces/jars/xerces-2.3.0.jar
XMLAPI=$MAVEN/xml-apis/jars/xml-apis-1.0.b2.jar
HSQLDB=$MAVEN/hsqldb/jars/hsqldb-1.7.1.jar
SOD=$MAVEN/sod/jars/sod-1.0Beta.jar

FULLPATH=${SEEDCODEC}${SEP}${SOD}${SEP}${FISSURESIDL}${SEP}${FISSURESIMPL}${SEP}${FISSURESUTIL}${SEP}${OB}${SEP}${OBNAMING}${SEP}${XERCES}${SEP}${XMLAPI}${SEP}${TAUP}${SEP}${LOG4J}${SEP}${HSQLDB}${SEP}${CLASSPATH} 

java -cp ${FULLPATH} edu.sc.seis.sod.Start $*
