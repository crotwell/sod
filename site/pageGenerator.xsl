<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect" extension-element-prefixes="redirect">
        <xsl:output method="html"/>
        <xsl:template match="/pages">
                <xsl:for-each select="page">
                        <redirect:write select="concat(../baseDirectory/text(), destination/text())">
                                <xsl:apply-templates select="document(source/text())"/>
                        </redirect:write>
                </xsl:for-each>
        </xsl:template>
        <xsl:template match="/document">
                <html>
                        <xsl:call-template name="head"/>
                        <body>
                                <xsl:call-template name="header"/>
                                <xsl:call-template name="menu"/>
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
                                <xsl:value-of select="@name"/>
                        </h3>
                        <xsl:apply-templates select="*" mode="sub"/>
                </div>
        </xsl:template>
        <xsl:template match="section" mode="sub">
                <div class="subsection">
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
                                <xsl:value-of select="document/properties/title/text()"/>
                        </title>
                        <!-- compliance patch for Internet Explorer -->
                        <!--[if lt IE 7]>
		<link rel="stylesheet" href="ie7-html.css" type="text/css">
		<![endif]-->
                        <link rel="stylesheet" href="main.css" type="text/css"/>
                </head>
        </xsl:template>
        <xsl:template name="header">
                <div id="header">
                        <img class="left" src="sodtractor100.jpg"/>
                        <img class="right" src="seisheader100.jpg"/>
                </div>
        </xsl:template>
        <xsl:template name="footer">
                <div id="footer">
                        <p>SOD is made possible by the <a href="http://www.sc.edu">University of
                                        South Carolina's</a>
                                <a href="http://www.geol.sc.edu"> Department of Geological
                                Sciences</a> and the <a href="http://www.iris.edu">IRIS Consortium</a>.</p>
                </div>
        </xsl:template>
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
