#!/bin/sh
echo 'Generating html from xml'
java org.apache.xalan.xslt.Process -in allPages.xml -xsl allPageGenerator.xsl
echo 'Copying over included stuff'
cp -r include/* generatedSite
rm -r generatedSite/CVS
cd schemaDocs
../../../devTools/maven/sod.py --docs
cd ..
echo 'Site now complete in generatedSite'
