#!/bin/sh

JAVA=java
if [ -z "${SOD_HOME}" ] ; then
SOD_HOME=.
fi
LIB=${SOD_HOME}/lib

ISTI_UTIL=${LIB}/isti/jars/isti.util-1.0.1USC.jar
ISTI_UTIL_TOPLEVEL=${LIB}/isti/jars/isti.util.toplevel-1.0USC.jar
JDOM=${LIB}/jdom/jars/jdom-b9.jar
JING=${LIB}/jing/jars/jing-20030619.jar
OPENMAP=${LIB}/openmap/jars/openmap-4.6.jar
JCALENDAR=${LIB}/jcalendar/jars/jcalendar-0.76.jar
JACORB=${LIB}/JacORB/jars/JacORB-2.1.jar
IDL=${LIB}/JacORB/jars/idl-2.1.jar
ANTLR=${LIB}/JacORB/jars/antlr-2.7.2.jar
AVALON_FRAMEWORK=${LIB}/JacORB/jars/avalon-framework-4.1.5.jar
CONCURRENT=${LIB}/JacORB/jars/concurrent-1.3.2.jar
LOGKIT=${LIB}/JacORB/jars/logkit-1.2.jar
FISSURESUTIL=${LIB}/fissuresUtil/jars/fissuresUtil-1.0.7beta.jar
FISSURESIMPL=${LIB}/fissuresImpl/jars/fissuresImpl-1.1.5beta.jar
FISSURESIDL=${LIB}/fissuresIDL/jars/fissuresIDL-1.0.jar
SEEDCODEC=${LIB}/SeedCodec/jars/SeedCodec-1.0Beta2.jar
TAUP=${LIB}/TauP/jars/TauP-1.1.4.jar
MOCKFISSURES=${LIB}/mockFissures/jars/mockFissures-0.2.jar
JUNIT_ADDONS=${LIB}/junit-addons/jars/junit-addons-1.3.jar
JUNIT=${LIB}/junit/jars/junit-3.8.1.jar
HSQLDB=${LIB}/hsqldb/jars/hsqldb-1.7.2-rc6d.jar
XERCES=${LIB}/xerces/jars/xerces-2.6.2.jar
XML_APIS=${LIB}/xml-apis/jars/xml-apis-2.6.2.jar
XALAN=${LIB}/xalan/jars/xalan-2.6.0.jar
LOG4J=${LIB}/log4j/jars/log4j-1.2.8.jar
RNGCONV=${LIB}/rngconv/jars/rngconv-20030225.jar
MSV=${LIB}/msv/jars/msv-20030807.jar
ISORELAX=${LIB}/msv/jars/isorelax-20030807.jar
RELAXNGDATATYPE=${LIB}/msv/jars/relaxngDatatype-20030807.jar
XSDLIB=${LIB}/msv/jars/xsdlib-20030807.jar
VELOCITY=${LIB}/velocity/jars/velocity-1.4.jar
COMMONS_COLLECTIONS=${LIB}/commons-collections/jars/commons-collections-3.0.jar
VELOCITY_TOOLS_GENERIC=${LIB}/velocity-tools/jars/velocity-tools-generic-1.1-rc1.jar
JAX_QNAME=${LIB}/stax/jars/jax-qname-1.0.jar
JSR173_API=${LIB}/stax/jars/jsr173_api-1.0.jar
JSR173_RI=${LIB}/stax/jars/jsr173_ri-1.0.jar
NAMESPACE=${LIB}/stax/jars/namespace-1.0.jar
SOD=${LIB}/sod/jars/sod-1.0Beta.jar

CLASSPATH=${ISTI_UTIL}:${ISTI_UTIL_TOPLEVEL}:${JDOM}:${JING}:${OPENMAP}:${JCALENDAR}:${JACORB}:${IDL}:${ANTLR}:${AVALON_FRAMEWORK}:${CONCURRENT}:${LOGKIT}:${FISSURESUTIL}:${FISSURESIMPL}:${FISSURESIDL}:${SEEDCODEC}:${TAUP}:${MOCKFISSURES}:${JUNIT_ADDONS}:${JUNIT}:${HSQLDB}:${XERCES}:${XML_APIS}:${XALAN}:${LOG4J}:${RNGCONV}:${MSV}:${ISORELAX}:${RELAXNGDATATYPE}:${XSDLIB}:${VELOCITY}:${COMMONS_COLLECTIONS}:${VELOCITY_TOOLS_GENERIC}:${JAX_QNAME}:${JSR173_API}:${JSR173_RI}:${NAMESPACE}:${SOD}


${JAVA} -Xmx256m \
-Djacorb.connection.client.pending_reply_timeout=120000 \
-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
-Djava.endorsed.dirs=${LIB}/JacOrb/jars \
-cp ${CLASSPATH} edu.sc.seis.sod.editor.SodGUIEditor $*