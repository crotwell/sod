<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns="http://relaxng.org/ns/structure/1.0"
  xmlns:rng="http://relaxng.org/ns/structure/1.0"
  xmlns:xsd="http://http://www.w3.org/2001/XMLSchema" >

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

<xsl:template match="rng:externalRef">
    <xsl:element name="ref" >
        <xsl:attribute name="name">
            <xsl:value-of select="translate(@href, './','')"/>
        </xsl:attribute> 
        <xsl:apply-templates/>
    </xsl:element>
</xsl:template>



<xsl:template match="/rng:grammar">
    <xsl:element name="{local-name()}" namespace="http://relaxng.org/ns/structure/1.0">
        <xsl:copy-of select="namespace::*"/>
        <xsl:apply-templates/>
        <xsl:for-each select="*//rng:externalRef">
            <xsl:element name="rng:define">
                <xsl:attribute name="name">
                    <xsl:value-of select="translate(@href, './','')"/>
                </xsl:attribute> 
                <xsl:text>Put file contents here on first visit</xsl:text>
            </xsl:element>
        </xsl:for-each>
    </xsl:element>
</xsl:template>

<!-- identity template without namespace nodes -->
<xsl:template match="*">
    <xsl:element name="{name()}">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
</xsl:template>

<xsl:template match="@*|text()|comment()|processing-instruction()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>
