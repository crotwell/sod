echo 'Generating html from xml'
java org.apache.xalan.xslt.Process -in allPages.xml -xsl pageGenerator.xsl
echo 'Copying over included stuff'
cp -r include/* generatedSite
echo 'Site now complete in generatedSite'
