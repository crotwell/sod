#!/bin/sh
MAVEN=${HOME}/.maven/repository
SEP=' '

#if using cygwin, use windows friendly options
cygwin=false;
case "`uname`" in
        CYGWIN*) cygwin=true;;
esac

while [ $# -ge 1 ]; do
        if [ "$1" = "-seperator" ]; then
                shift
                SEP=$1
                shift
        fi
        if [ "$1" = "-repository" ]; then
                shift
                MAVEN=$1
                shift
        fi
        if [ "$1" = "-nocygpath" ]; then
                shift
                cygwin=false;
                shift
        fi
done
if $cygwin ; then
        if [ "$SEP" = ":" ]; then
                SEP="\;"
                MAVEN=`cygpath -w $MAVEN`
        fi
fi

MSV=${MAVEN}/msv/jars/msv-20030807.jar
RELAXDATA=${MAVEN}/msv/jars/relaxngDatatype-20030807.jar
XSD=${MAVEN}/msv/jars/xsdlib-20030807.jar
XERCES=${MAVEN}/xerces/jars/xerces-2.4.0.jar
ISORELAX=${MAVEN}/msv/jars/isorelax-20030807.jar
RNGCONV=${MAVEN}/rngconv/jars/rngconv-20030225.jar
SOD=${MAVEN}/sod/jars/sod-2.0beta.jar
FULLPATH=${MSV}${SEP}${RELAXDATA}${SEP}${XSD}${SEP}${XERCES}${SEP}${ISORELAX}${SEP}${RNGCONV}${SEP}${SOD}

echo $FULLPATH
exit 0
