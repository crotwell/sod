<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:include href="menuGenerator.xsl"/>
    <xsl:param name="base"/>
    <xsl:param name="menu" select="'Tutorials'"/>
    <xsl:param name="page"/>
    <xsl:template match="/">
        <xsl:param name="currentPage" select="$page"/>
        <xsl:apply-templates select="*">
            <xsl:with-param name="currentPage" select="$currentPage"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="/document">
        <xsl:param name="currentPage"/>
        <html>
            <xsl:call-template name="head"/>
            <body>
                <xsl:call-template name="header"/>
                <xsl:call-template name="menu">
                    <xsl:with-param name="currentPage" select="$currentPage"/>
                    <xsl:with-param name="base" select="$base"/>
                    <xsl:with-param name="menu" select="$menu"/>
                </xsl:call-template>
                <div id="content">
                    <xsl:apply-templates select="body/*"/>
                </div>
                <xsl:call-template name="footer"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="section">
        <div class="section">
            <h3>
                <a>
                    <xsl:attribute name="name">
                        <xsl:value-of select="@name"/>
                    </xsl:attribute>
                    <xsl:value-of select="@name"/>
                </a>
            </h3>
            <xsl:apply-templates select="*" mode="sub"/>
        </div>
    </xsl:template>
    <xsl:template match="section" mode="sub">
        <div class="section">
            <h4>
                <xsl:value-of select="@name"/>
            </h4>
            <xsl:apply-templates select="*" mode="sub"/>
        </div>
    </xsl:template>
    <xsl:template match="source">
        <pre>
            <xsl:copy-of select="text()"/>
        </pre>
    </xsl:template>
    <xsl:template match="source" mode="sub">
        <pre>
            <xsl:copy-of select="text()"/>
        </pre>
    </xsl:template>
    <xsl:template match="*">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template match="*" mode="sub">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template name="head">
        <head>
            <title>
                <xsl:value-of select="properties/title/text()"/>
            </title>
            <!-- compliance patch for Internet Explorer -->
            <!--[if lt IE 7]>
		<link rel="stylesheet" href="ie7-html.css" type="text/css">
		<![endif]-->
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($base, 'main.css')"/>
                </xsl:attribute>
            </link>
        </head>
    </xsl:template>
    <xsl:template name="header">
        <div id="header">
            <a href="http:/seis.sc.edu/SOD">
                <img class="left">
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($base, 'images/full-sodlogo-y100.gif')"/>
                    </xsl:attribute>
                </img>
            </a>
            <a href="http://seis.sc.edu/">
                <img class="right">
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($base, 'seisheader100.jpg')"/>
                    </xsl:attribute>
                </img>
            </a>
        </div>
    </xsl:template>
    <xsl:template name="footer">
        <div id="footer">
            <p>SOD is made possible by the <a href="http://www.sc.edu">University of South Carolina's</a>
                <a href="http://www.geol.sc.edu"> Department of Geological Sciences</a> and the <a
                    href="http://www.iris.edu">IRIS Consortium</a>.</p>
        </div>
    </xsl:template>
</xsl:stylesheet>
