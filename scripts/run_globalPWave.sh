#!/bin/sh

SOD=lib/sod.jar
OB=lib/OB.jar
OBNAMING=lib/OBNaming.jar
FISSURESUTIL=lib/fissuresUtil.jar
FISSURESIMPL=lib/FissuresImpl.jar
FISSURESIDL=lib/FissuresIDL.jar
XERCES=lib/xercesImpl.jar
XMLAPI=lib/xml-apis.jar
XALAN=lib/xalan.jar
TAUP=lib/taup.jar
LOG4J=lib/log4j.jar
HSQLDB=lib/hsqldb.jar


java -cp ${SOD}:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${OB}:${OBNAMING}:${XERCES}:${XMLAPI}:${XALAN}:${TAUP}:${LOG4J}:${HSQLDB}:${CLASSPATH} edu.sc.seis.sod.Start -props globalPWave.prop

