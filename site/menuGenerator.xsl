<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:template name="menu">
                <xsl:param name="currentPage"/>
                <div id="menu">
                        <ul>
                                <xsl:apply-templates select="document('allPages.xml')/pages/page" mode="menuGeneration">
                                        <xsl:with-param name="currentPage" select="$currentPage"/>
                                </xsl:apply-templates>
                        </ul>
                </div>
        </xsl:template>
        <xsl:template match="page" mode="menuGeneration">
                <xsl:param name="currentPage"/>
                <li>
                        <xsl:if test="$currentPage = destination/text()">
                                <xsl:attribute name="id">
                                        <xsl:value-of select="'selected'"/>
                                </xsl:attribute>
                        </xsl:if>
                        <a>
                                <xsl:attribute name="href">
                                        <xsl:value-of select="destination/text()"/>
                                </xsl:attribute>
                                <xsl:value-of select="name/text()"/>
                        </a>
                </li>
        </xsl:template>
</xsl:stylesheet>
