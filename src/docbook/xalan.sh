#!/bin/bash

XALAN=../../../../External/xalan-j_2_6_0/bin

CP=${XALAN}/xalan.jar:${XALAN}/xml-apis.jar:${XALAN}/xercesImpl.jar

java -cp ${CP} org.apache.xalan.xslt.Process -IN tutorial.xml -OUT tutorial.xhtml -XSL ../../../../External/docbook-xsl-1.65.1/xhtml/docbook.xsl
echo "xhtml done"
java -cp ${CP} org.apache.xalan.xslt.Process -IN tutorial.xml -OUT tutorial.html -XSL ../../../../External/docbook-xsl-1.65.1/html/docbook.xsl
echo "html done"

java -cp ${CP} org.apache.xalan.xslt.Process -IN tutorial.xml -OUT tutorial.fo -XSL ../../../../External/docbook-xsl-1.65.1/fo/docbook.xsl
echo "fo done"
