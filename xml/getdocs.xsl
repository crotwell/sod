<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Menu Structure of GEE</title>
      </head>
      <body>
         <xsl:apply-templates select="/xsd:complexType" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xsd:complexType" >
    <p>
      <xsl:apply-templates select="/xsd:sequence" />
    </p>
  </xsl:template>

  <xsl:template match="xsd:sequence" >
    <p>
      <xsl:apply-templates select="/xsd:element" />
      <br/>
    </p>
  </xsl:template>

  <xsl:template match="xsd:element" >
    <p>
      name = <xsl:value-of select="@name" />
      type = <xsl:value-of select="@type" />
      <br/>
    </p>
  </xsl:template>

</xsl:stylesheet>

