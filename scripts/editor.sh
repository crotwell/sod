#!/bin/sh
MAVEN=~/.maven

JACORB=$MAVEN/repository/JacORB/jars/JacORB-1.4.1.jar
# for orbacus
OB=$MAVEN/repository/OB/jars/OB-4.1.0.jar
OBNAMING=$MAVEN/repository/OBNaming/jars/OBNaming-4.1.0.jar

SEEDCODEC=$MAVEN/repository/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/repository/fissuresUtil/jars/fissuresUtil-1.0.2beta.jar
FISSURESIMPL=$MAVEN/repository/fissuresImpl/jars/fissuresImpl-1.1.jar
FISSURESIDL=$MAVEN/repository/fissuresIDL/jars/fissuresIDL-1.0.jar
GEOTOOLS=$MAVEN/repository/Geotools1/jars/geotools1.2.jar
LOG4J=$MAVEN/repository/log4j/jars/log4j-1.2.6.jar
TAUP=$MAVEN/repository/TauP/jars/TauP-1.1.4.jar
XALAN=$MAVEN/repository/xalan/jars/xalan-2.5.1.jar
XERCES=$MAVEN/repository/xerces/jars/xerces-2.4.0.jar
XMLAPI=$MAVEN/repository/xml-apis/jars/xml-apis-1.0.b2.jar
JAICORE=$MAVEN/repository/jars/jai_core.jar
JAICODEC=$MAVEN/repository/jars/jai_codec.jar
HSQLDB=$MAVEN/repository/hsqldb/jars/hsqldb-1.7.1.jar
SOD=$MAVEN/repository/sod/jars/sod-1.0Beta.jar


# command line text only
#java -cp ${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.editor.CommandLineEditor $*

# simple gui editor
#java -cp ${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.editor.SimpleGUIEditor $*

# sod gui editor
java -cp ${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.editor.SodGUIEditor $*

