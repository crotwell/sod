#!/bin/sh
MAVEN=~/.maven

JACORB_LIB=$MAVEN/repository/JacORB/jars
JACORB=$JACORB_LIB/JacORB-2.0.jar
JACORB_ANTLR=$JACORB_LIB/antlr-2.7.2.jar
JACORB_AVALON=$JACORB_LIB/avalon-framework-4.1.5.jar
JACORB_CONCURRENT=$JACORB_LIB/concurrent-1.3.2.jar
JACORB_LOGKIT=$JACORB_LIB/logkit-1.2.jar



SEEDCODEC=$MAVEN/repository/SeedCodec/jars/SeedCodec-1.0Beta.jar
FISSURESUTIL=$MAVEN/repository/fissuresUtil/jars/fissuresUtil-1.0.6beta.jar
FISSURESIMPL=$MAVEN/repository/fissuresImpl/jars/fissuresImpl-1.1.4beta.jar
FISSURESIDL=$MAVEN/repository/fissuresIDL/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/repository/log4j/jars/log4j-1.2.8.jar
TAUP=$MAVEN/repository/TauP/jars/TauP-1.1.4.jar
XALAN=$MAVEN/repository/xalan/jars/xalan-2.5.1.jar
XERCES=$MAVEN/repository/xerces/jars/xerces-2.4.0.jar
XMLAPI=$MAVEN/repository/xml-apis/jars/xml-apis-1.0.b2.jar
JAICORE=$MAVEN/repository/jars/jai_core.jar
JAICODEC=$MAVEN/repository/jars/jai_codec.jar
HSQLDB=$MAVEN/repository/hsqldb/jars/hsqldb-20040212.jar
OPENMAP=$MAVEN/repository/openmap/jars/openmap-4.5.4.jar
JING=$MAVEN/repository/jing/jars/jing-20030619.jar
SOD=$MAVEN/repository/sod/jars/sod-1.0Beta.jar


java -Djava.endorsed.dirs=${JACORB_LIB}  \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
    -Djacorb.connection.client.pending_reply_timeout=120000 \
    -Xmx256m \
    -cp ${JACORB}:${JACORB_ANTLR}:${JACORB_AVALON}:${JACORB_CONCURRENT}:${JACORB_LOGKIT}:${JING}:${OPENMAP}:${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} \
    edu.sc.seis.sod.Start $*

