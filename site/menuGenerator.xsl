<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="menu">
        <xsl:param name="currentPage"/>
        <xsl:param name="base"/>
        <xsl:param name="menu" select="'Tutorials'"/>
        <div id="menu">
            <xsl:for-each select="document('allPages.xml')/pages/menu">
                <xsl:if test="@name = $menu">
                    <ul>
                        <xsl:apply-templates select="*" mode="menuGeneration">
                            <xsl:with-param name="currentPage" select="$currentPage"/>
                            <xsl:with-param name="base" select="$base"/>
                        </xsl:apply-templates>
                    </ul>
                </xsl:if>
            </xsl:for-each>
        </div>
    </xsl:template>
    <xsl:template match="page" mode="menuGeneration">
        <xsl:param name="currentPage"/>
        <xsl:param name="base"/>
        <li>
            <xsl:if test="$currentPage = destination/text()">
                <xsl:attribute name="id">
                    <xsl:value-of select="'selected'"/>
                </xsl:attribute>
            </xsl:if>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($base, destination/text())"/>
                </xsl:attribute>
                <xsl:value-of select="name/text()"/>
            </a>
        </li>
    </xsl:template>
    <xsl:template match="submenu" mode="menuGeneration">
        <xsl:param name="currentPage"/>
        <xsl:param name="base"/>
        <ul class="indent">
            <xsl:if test="position() = last()">
                <xsl:attribute name="class">last indent</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="*" mode="menuGeneration">
                <xsl:with-param name="currentPage" select="$currentPage"/>
                <xsl:with-param name="base" select="$base"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>
</xsl:stylesheet>
