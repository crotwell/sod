<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:template name="menu">
                <div id="menu">
                        <ul>
                                <xsl:apply-templates select="document('allPages.xml')/pages/page" mode="menuGeneration"/>
                        </ul>
                </div>
        </xsl:template>
        <xsl:template match="page" mode="menuGeneration">
                <li>
                        <a>
                                <xsl:attribute name="href"><xsl:value-of select="destination/text()"/></xsl:attribute>
                                <xsl:value-of select="name/text()"/>
                        </a>
                </li>
        </xsl:template>
</xsl:stylesheet>
