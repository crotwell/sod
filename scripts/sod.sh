#!/bin/sh
MAVEN=~/.maven/repository

# timeout in milliseconds, use large enough number to avoid thrashing the server
JACORB_TIMEOUT=900000

JACORB_LIB=$MAVEN/jars
JACORB=$JACORB_LIB/JacORB-2.1.jar
JACORB_ANTLR=$JACORB_LIB/antlr-2.7.2.jar
JACORB_AVALON=$JACORB_LIB/avalon-framework-4.1.5.jar
JACORB_CONCURRENT=$JACORB_LIB/concurrent-1.3.2.jar
JACORB_LOGKIT=$JACORB_LIB/logkit-1.2.jar


SEEDCODEC=$MAVEN/jars/SeedCodec-1.0Beta2.jar
FISSURESUTIL=$MAVEN/jars/fissuresUtil-1.0.7beta.jar
FISSURESIMPL=$MAVEN/jars/fissuresImpl-1.1.5beta.jar
FISSURESIDL=$MAVEN/jars/fissuresIDL-1.0.jar
LOG4J=$MAVEN/jars/log4j-1.2.8.jar
TAUP=$MAVEN/jars/TauP-1.1.4.jar
XALAN=$MAVEN/jars/xalan-2.5.1.jar
XERCES=$MAVEN/jars/xerces-2.4.0.jar
XMLAPI=$MAVEN/apis/jars/xml-apis-1.0.b2.jar
JAICORE=$MAVEN/jai_core.jar
JAICODEC=$MAVEN/jai_codec.jar
HSQLDB=$MAVEN/jars/hsqldb-20040212.jar
OPENMAP=$MAVEN/jars/openmap-4.6.jar
JING=$MAVEN/jars/jing-20030619.jar
VELOCITY=$MAVEN/jars/velocity-1.4-rc1.jar
VELOCITY_TOOLS=$MAVEN/tools/jars/velocity-tools-generic-1.1-rc1.jar
COMMONS_COLL=$MAVEN/collections/jars/commons-collections-3.0.jar
SOD=$MAVEN/jars/sod-1.0Beta.jar


java -Djava.endorsed.dirs=${JACORB_LIB}  \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
    -Djacorb.connection.client.pending_reply_timeout=${JACORB_TIMEOUT} \
    -Xmx512m \
    -cp ${JACORB}:${JACORB_ANTLR}:${JACORB_AVALON}:${JACORB_CONCURRENT}:${JACORB_LOGKIT}:${VELOCITY}:${COMMONS_COLL}:${VELOCITY_TOOLS}:${JING}:${OPENMAP}:${SEEDCODEC}:${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} \
    edu.sc.seis.sod.Start $*

