#!/bin/sh

JAVA=java
SOD_HOME=~/.maven
PROP=cwg.prop

REPOSITORY=${SOD_HOME}/repository

JING=${REPOSITORY}/jing/jars/jing-20030619.jar
OPENMAP=${REPOSITORY}/openmap/jars/openmap-4.6.jar
JCALENDAR=${REPOSITORY}/jcalendar/jars/jcalendar-0.6.1beta.jar
JACORB=${REPOSITORY}/JacORB/jars/JacORB-2.1.jar
IDL=${REPOSITORY}/JacORB/jars/idl-2.1.jar
ANTLR=${REPOSITORY}/JacORB/jars/antlr-2.7.2.jar
AVALON_FRAMEWORK=${REPOSITORY}/JacORB/jars/avalon-framework-4.1.5.jar
CONCURRENT=${REPOSITORY}/JacORB/jars/concurrent-1.3.2.jar
LOGKIT=${REPOSITORY}/JacORB/jars/logkit-1.2.jar
FISSURESUTIL=${REPOSITORY}/fissuresUtil/jars/fissuresUtil-1.0.7beta.jar
FISSURESIMPL=${REPOSITORY}/fissuresImpl/jars/fissuresImpl-1.1.5beta.jar
FISSURESIDL=${REPOSITORY}/fissuresIDL/jars/fissuresIDL-1.0.jar
SEEDCODEC=${REPOSITORY}/SeedCodec/jars/SeedCodec-1.0Beta2.jar
TAUP=${REPOSITORY}/TauP/jars/TauP-1.1.4.jar
MOCKFISSURES=${REPOSITORY}/mockFissures/jars/mockFissures-0.2.jar
JUNIT_ADDONS=${REPOSITORY}/junit-addons/jars/junit-addons-1.3.jar
JUNIT=${REPOSITORY}/junit/jars/junit-3.8.1.jar
MCKOI=${REPOSITORY}/mckoi/jars/mckoi-1.0.2.jar
HSQLDB=${REPOSITORY}/hsqldb/jars/hsqldb-1.7.2-rc5.jar
XERCES=${REPOSITORY}/xerces/jars/xerces-2.3.0.jar
XML_APIS=${REPOSITORY}/xml-apis/jars/xml-apis-2.0.2.jar
XALAN=${REPOSITORY}/xalan/jars/xalan-2.3.1.jar
EASYMOCK=${REPOSITORY}/easyMock/jars/easyMock-1.0.jar
LOG4J=${REPOSITORY}/log4j/jars/log4j-1.2.8.jar
RNGCONV=${REPOSITORY}/rngconv/jars/rngconv-20030225.jar
MSV=${REPOSITORY}/msv/jars/msv-20030807.jar
ISORELAX=${REPOSITORY}/msv/jars/isorelax-20030807.jar
RELAXNGDATATYPE=${REPOSITORY}/msv/jars/relaxngDatatype-20030807.jar
XSDLIB=${REPOSITORY}/msv/jars/xsdlib-20030807.jar
VELOCITY=${REPOSITORY}/velocity/jars/velocity-1.4-rc1.jar
COMMONS_COLLECTIONS=${REPOSITORY}/commons-collections/jars/commons-collections-3.0.jar
VELOCITY_TOOLS_GENERIC=${REPOSITORY}/velocity-tools/jars/velocity-tools-generic-1.1-rc1.jar
SOD=${REPOSITORY}/sod/jars/sod-1.0Beta.jar

CLASSPATH=${JING}:${OPENMAP}:${JCALENDAR}:${JACORB}:${IDL}:${ANTLR}:${AVALON_FRAMEWORK}:${CONCURRENT}:${LOGKIT}:${FISSURESUTIL}:${FISSURESIMPL}:${FISSURESIDL}:${SEEDCODEC}:${TAUP}:${MOCKFISSURES}:${JUNIT_ADDONS}:${JUNIT}:${MCKOI}:${HSQLDB}:${XERCES}:${XML_APIS}:${XALAN}:${EASYMOCK}:${LOG4J}:${RNGCONV}:${MSV}:${ISORELAX}:${RELAXNGDATATYPE}:${XSDLIB}:${VELOCITY}:${COMMONS_COLLECTIONS}:${VELOCITY_TOOLS_GENERIC}:${SOD}:.

${JAVA} \
-Xmx128m \
-Djava.endorsed.dirs=${REPOSITORY}/JacOrb/jars \
-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
-Djacorb.connection.client.pending_reply_timeout=120000 \
-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
-cp ${CLASSPATH} edu.sc.seis.sod.Start \
$*
