#!/bin/sh
JACORB=$MAVEN_HOME/repository/JacORB/jars/JacORB-1.4.1.jar
# for orbacus
OB=$MAVEN_HOME/repository/OB/jars/OB-4.1.0.jar
OBNAMING=$MAVEN_HOME/repository/OBNaming/jars/OBNaming-4.1.0.jar

SEEDCODEC=$MAVEN_HOME/repository/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN_HOME/repository/fissuresUtil/jars/fissuresUtil-1.0Beta.jar
FISSURESIMPL=$MAVEN_HOME/repository/fissuresImpl/jars/fissuresImpl-1.1Beta.jar
FISSURESIDL=$MAVEN_HOME/repository/fissuresIDL/jars/fissuresIDL-1.0.jar
GEOTOOLS=$MAVEN_HOME/repository/Geotools1/jars/geotools1.2.jar
LOG4J=$MAVEN_HOME/repository/log4j/jars/log4j-1.2.6.jar
TAUP=$MAVEN_HOME/repository/TauP/jars/TauP-1.1.4.jar
XALAN=$MAVEN_HOME/repository/xalan/jars/xalan-2.4.1.jar
XERCES=$MAVEN_HOME/repository/xerces/jars/xerces-2.0.2.jar
XMLAPI=$MAVEN_HOME/repository/xml-apis/jars/xml-apis-1.0.b2.jar
JAICORE=$MAVEN_HOME/repository/jars/jai_core.jar
JAICODEC=$MAVEN_HOME/repository/jars/jai_codec.jar
HSQLDB=$MAVEN_HOME/repository/hsqldb/jars/hsqldb-1.7.1.jar
SOD=../target/sod-1.0Beta.jar


java -cp ${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${OB}:${OBNAMING}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.Start -f USNSN.xml
#java -cp ${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${OB}:${OBNAMING}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.Start -props USNSN.prop -f USNSN.xml

