echo 'Generating html from xml'
java org.apache.xalan.xslt.Process -in allPages.xml -xsl allPageGenerator.xsl
echo 'Copying over included stuff'
cp include/* generatedSite
echo 'Site now complete in generatedSite'
