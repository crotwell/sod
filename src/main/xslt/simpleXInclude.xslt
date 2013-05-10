<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns="http://relaxng.org/ns/structure/1.0"
  xmlns:rng="http://relaxng.org/ns/structure/1.0"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:xsd="http://http://www.w3.org/2001/XMLSchema" >

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

<xsl:template match="/rng:grammar">
    <xsl:element name="{local-name()}" >
        <xsl:copy-of select="namespace::*"/>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>

        <xsl:for-each select="//xi:include">
                <xsl:comment>Include from <xsl:value-of select="@href"/></xsl:comment> 

            <xsl:apply-templates select="document(@href)/rng:grammar/rng:define" />
                <xsl:comment>End Include from <xsl:value-of select="@href"/></xsl:comment> 
        </xsl:for-each>
    </xsl:element>
</xsl:template>

<xsl:template match="xi:include"/>

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
