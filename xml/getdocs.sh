#!/bin/sh

XALANDIR=/Users/crotwell/Developement/xalan-j_2_4_0/bin 

java -cp ${XALANDIR}/BCEL.jar:${XALANDIR}/JLex.jar:${XALANDIR}/bsf.jar:${XALANDIR}/java_cup.jar:${XALANDIR}/regexp.jar:${XALANDIR}/runtime.jar:${XALANDIR}/xalan.jar:${XALANDIR}/xalansamples.jar:${XALANDIR}/xalanservlet.jar:${XALANDIR}/xercesImpl.jar:${XALANDIR}/xml-apis.jar:${XALANDIR}/xsltc.jar org.apache.xalan.xslt.Process -in sod.xsd -out sod_docs.html -xsl getdocs.xsl

echo Done!


