#!/bin/sh

OB=../../OrbacusSignedJars/dist/OB.jar
OBNAMING=../../OrbacusSignedJars/dist/OBNaming.jar
FISSURESUTIL=../../fissuresUtil/dist/lib/fissuresUtil.jar
FISSURESIMPL=../../fissures/dist/lib/FissuresImpl.jar
FISSURESIDL=../../fissures/dist/lib/FissuresIDL.jar
TAUP=../../TauP/taup.jar
XERCES=../../Xerces/xercesImpl.jar
XMLAPI=../../Xerces/xmlParserAPIs.jar
LOG4J=../../jakarta-log4j-1.1.3/dist/lib/log4j.jar


java -cp ../build:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${OB}:${OBNAMING}:${TAUP}:${XERCES}:${XMLAPI}:${LOG4J} edu.sc.seis.sod.Start $*
