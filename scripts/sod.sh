#!/bin/sh

JAVA=java
if [ -z "${SOD_HOME}" ] ; then
SOD_HOME=.
fi
LIB=${SOD_HOME}/lib

ISTI_UTIL=${LIB}/isti.util-1.0.1USC.jar
ISTI_UTIL_TOPLEVEL=${LIB}/isti.util.toplevel-1.0USC.jar
JDOM=${LIB}/jdom-b9.jar
JING=${LIB}/jing-20030619.jar
OPENMAP=${LIB}/openmap-4.6.jar
JCALENDAR=${LIB}/jcalendar-0.76.jar
JACORB=${LIB}/JacORB-2.1.jar
IDL=${LIB}/idl-2.1.jar
ANTLR=${LIB}/antlr-2.7.2.jar
AVALON_FRAMEWORK=${LIB}/avalon-framework-4.1.5.jar
CONCURRENT=${LIB}/concurrent-1.3.2.jar
LOGKIT=${LIB}/logkit-1.2.jar
FISSURESUTIL=${LIB}/fissuresUtil-1.0.8beta.jar
FISSURESIMPL=${LIB}/fissuresImpl-1.1.6beta.jar
FISSURESIDL=${LIB}/fissuresIDL-1.0.jar
SEEDCODEC=${LIB}/SeedCodec-1.0beta2.jar
TAUP=${LIB}/TauP-1.1.5beta.jar
MOCKFISSURES=${LIB}/mockFissures-0.3.jar
JUNIT_ADDONS=${LIB}/junit-addons-1.3.jar
JUNIT=${LIB}/junit-3.8.1.jar
HSQLDB=${LIB}/hsqldb-1.7.2.jar
XERCES=${LIB}/xerces-2.6.2.jar
XML_APIS=${LIB}/xml-apis-2.6.2.jar
XALAN=${LIB}/xalan-2.6.0.jar
LOG4J=${LIB}/log4j-1.2.8.jar
RNGCONV=${LIB}/rngconv-20030225.jar
MSV=${LIB}/msv-20030807.jar
ISORELAX=${LIB}/isorelax-20030807.jar
RELAXNGDATATYPE=${LIB}/relaxngDatatype-20030807.jar
XSDLIB=${LIB}/xsdlib-20030807.jar
VELOCITY=${LIB}/velocity-1.4.jar
COMMONS_COLLECTIONS=${LIB}/commons-collections-3.0.jar
VELOCITY_TOOLS_GENERIC=${LIB}/velocity-tools-generic-1.1-rc1.jar
JAX_QNAME=${LIB}/jax-qname-1.0.jar
JSR173_API=${LIB}/jsr173_api-1.0.jar
JSR173_RI=${LIB}/jsr173_ri-1.0.jar
NAMESPACE=${LIB}/namespace-1.0.jar
ETOPO10=${LIB}/etopo10.jar
SOD=${LIB}/sod-2.0beta.jar

CLASSPATH=${ISTI_UTIL}:${ISTI_UTIL_TOPLEVEL}:${JDOM}:${JING}:${OPENMAP}:${JCALENDAR}:${JACORB}:${IDL}:${ANTLR}:${AVALON_FRAMEWORK}:${CONCURRENT}:${LOGKIT}:${FISSURESUTIL}:${FISSURESIMPL}:${FISSURESIDL}:${SEEDCODEC}:${TAUP}:${MOCKFISSURES}:${JUNIT_ADDONS}:${JUNIT}:${HSQLDB}:${XERCES}:${XML_APIS}:${XALAN}:${LOG4J}:${RNGCONV}:${MSV}:${ISORELAX}:${RELAXNGDATATYPE}:${XSDLIB}:${VELOCITY}:${COMMONS_COLLECTIONS}:${VELOCITY_TOOLS_GENERIC}:${JAX_QNAME}:${JSR173_API}:${JSR173_RI}:${NAMESPACE}:${ETOPO10}:${SOD}


${JAVA} -Xmx256m \
-Djacorb.connection.client.pending_reply_timeout=120000 \
-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
-Djava.endorsed.dirs=${LIB}/JacOrb/jars \
-cp ${CLASSPATH} edu.sc.seis.sod.Start $*