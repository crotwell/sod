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
HSQLDB=$MAVEN_HOME/repository/hsqldb/jars/hsqldb-1.7.1.jar
SOD=$MAVEN_HOME/repository/sod/jars/sod-1.0Beta.jar

# use JacOrb
#java -cp ${GEE}:${CLASSICS}:${XMLAPI}:${XERCES}:${XALAN}:${TAUP}:${LOG4J}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${GEOTOOLS}:${JACORB}:${JAICORE}:${JAICODEC}:${CLASSPATH} edu.sc.seis.vsnexplorer.Start -props ./alpha.prop

# use orbacus
java -cp ${HSQLDB}:${COMPRESS}:${XMLAPI}:${XERCES}:${XALAN}:${TAUP}:${LOG4J}:${SEEDCODEC}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${GEOTOOLS}:${OB}:${OBNAMING}:${SOD}:${CLASSPATH} edu.sc.seis.sod.Start  -props ./globalPWave.prop -f globalPWave.xml


