#!/bin/sh

XALANDIR=/Users/crotwell/External/xalan-j_2_4_1/bin 

java -Xbootclasspath/p:${XALANDIR}/xalan.jar:${XALANDIR}/xercesImpl.jar:${XALANDIR}/xml-apis.jar -cp ${XALANDIR}/BCEL.jar:${XALANDIR}/JLex.jar:${XALANDIR}/bsf.jar:${XALANDIR}/java_cup.jar:${XALANDIR}/regexp.jar:${XALANDIR}/runtime.jar:${XALANDIR}/xalansamples.jar:${XALANDIR}/xalanservlet.jar:${XALANDIR}/xsltc.jar org.apache.xalan.xslt.EnvironmentCheck


java -Xbootclasspath/p:${XALANDIR}/xalan.jar:${XALANDIR}/xercesImpl.jar:${XALANDIR}/xml-apis.jar -cp ${XALANDIR}/BCEL.jar:${XALANDIR}/JLex.jar:${XALANDIR}/bsf.jar:${XALANDIR}/java_cup.jar:${XALANDIR}/regexp.jar:${XALANDIR}/runtime.jar:${XALANDIR}/xalansamples.jar:${XALANDIR}/xalanservlet.jar:${XALANDIR}/xsltc.jar org.apache.xalan.xslt.Process -in ../../src/edu/sc/seis/sod/data/sod.xsd -out sod_docs.html -xsl getdocs.xsl 

echo Done!


