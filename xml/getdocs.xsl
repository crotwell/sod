<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema" >

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Menu Structure of GEE</title>
      </head>
      <body>
         <p>test</p>
         <xsl:apply-templates select="xsd:schema" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xsd:schema" >
      <xsl:apply-templates select="xsd:complexType" />
  </xsl:template>

  <xsl:template match="xsd:complexType" >
    <h1>
      <xsl:value-of select="@name" />
    </h1>
    <p>
      <xsl:apply-templates select="xsd:sequence" />
    </p>
  </xsl:template>

  <xsl:template match="xsd:sequence" >
    <p>
      <xsl:apply-templates select="xsd:element" />
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

