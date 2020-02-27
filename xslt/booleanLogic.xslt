<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="xmlverbatim.xsl"/>
    <xsl:output method="html"/>
    <xsl:template match="/">
        <xsl:apply-templates select="sod/eventArm/originOR"/>
    </xsl:template>
    <xsl:template match="originOR">
    &lt;originOR&gt;<span class="red"><xsl:text>&#xa;            </xsl:text>
    <xsl:apply-templates select="originAND" mode="inOriginOR"/></span>
    <span class="blue"><xsl:text>&#xa;            </xsl:text>
    <xsl:apply-templates select="magnitudeRange" mode="inOriginOR"/></span>
    &lt;/originOR&gt;</xsl:template>
    <xsl:template match="*" mode="inOriginOR">
        <xsl:call-template name="xmlverb-element"/>
    </xsl:template>
</xsl:stylesheet>
