<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect" extension-element-prefixes="redirect">
    <xsl:output method="html"/>
    <xsl:include href="pageGenerator.xsl"/>
    <xsl:template match="/pages">
        <xsl:for-each select="menu">
            <xsl:for-each select="page">
                <xsl:if test="contains(source/text(), '.xml')">
                    <redirect:write select="concat(../../baseDirectory/text(), destination/text())">
                        <xsl:apply-templates select="document(source/text())">
                            <xsl:with-param name="currentPage" select="destination/text()"/>
                            <xsl:with-param name="base" select="base/text()"/>
                            <xsl:with-param name="menu">
                                <xsl:choose>
                                    <xsl:when test="menu">
                                        <xsl:value-of select="menu/text()"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="'Tutorials'"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                        </xsl:apply-templates>
                    </redirect:write>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
