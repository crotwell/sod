<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="xmlverbatim.xsl"/>
    <xsl:output method="html" indent="no"/>
    <!-- select the name of an element that should be formatted
        (print only these elements and their contents) -->
    <xsl:param name="select"/>
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$select">
                <xsl:text>        </xsl:text>
                <xsl:apply-templates mode="xmlverbwrapper"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>&lt;?xml version="1.0"?>&#xa;</xsl:text>
                <xsl:apply-templates select="." mode="xmlverb"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="*" mode="xmlverbwrapper">
        <xsl:choose>
            <xsl:when test="name()=$select">
                <xsl:apply-templates select="." mode="xmlverb"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- look for the selected element among the children -->
                <xsl:apply-templates select="*" mode="xmlverbwrapper"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
