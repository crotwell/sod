<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema" >

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Sod Tag Documentation</title>
      </head>
      <body>
         <p>test</p>
         <xsl:apply-templates select="xsd:schema" />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xsd:schema" >
    <p>Doing a schema</p>
    <xsl:apply-templates select="xsd:include" />
    <xsl:apply-templates select="xsd:complexType" />
  </xsl:template>

  <xsl:template match="xsd:include" >
    <p>Found an include <xsl:value-of select="@schemaLocation" /></p>
    <xsl:apply-templates select="document(@schemaLocation)/xsd:schema" />
  </xsl:template>

  <xsl:template match="xsd:complexType" >
    <xsl:if test="not(@abstract='true')" >
      <h1>
        <xsl:value-of select="@name" />
      </h1>
      <p>
        <xsl:apply-templates select="xsd:sequence|xsd:complexContent" />
      </p>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xsd:sequence" >
    <p>
      <xsl:apply-templates select="xsd:element" />
      <br/>
    </p>
  </xsl:template>

  <xsl:template match="xsd:complexContent" >
      <xsl:apply-templates select="xsd:extension" />
  </xsl:template>

  <xsl:template match="xsd:extension" >
    <p>doing an extesion</p>
    <xsl:apply-templates select="ancestor::xsd:complexType[@name=@base]" />
    <xsl:apply-templates select="xsd:sequence" />
  </xsl:template>

  <xsl:template match="xsd:element" >
    <p>
      name = <xsl:value-of select="@name" />
      type = <xsl:value-of select="@type" />
      <br/>
    </p>
  </xsl:template>

</xsl:stylesheet>

