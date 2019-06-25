<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="xmlverbatim.xsl"/>
    <xsl:output method="html"/>
    <xsl:template match="/">
        <xsl:apply-templates select="//printlineSeismogramProcess | //printlineSeismogramProcess/following-sibling::*"/>
    </xsl:template>
    <xsl:template match="*">
        <xsl:text>        </xsl:text>
        <xsl:call-template name="xmlverb-element"/>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
