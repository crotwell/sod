<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:output method="html"/>
        <xsl:template match="/">
                <html>
                        <xsl:call-template name="head"/>
                        <body>
                                <xsl:call-template name="header"/>
                                <div id="content">
                                <xsl:apply-templates select="document/body/*"/>
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
                        <xsl:apply-templates select="*"/>
                </div>
        </xsl:template>
        <xsl:template match="p">
                <xsl:copy-of select="."/>
        </xsl:template>
        <xsl:template name="head">
                <head>
                        <title><xsl:value-of select="document/properties/title/text()"/>
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
                        <h2>SOD</h2>
                </div>
                <div id="menu">
                        <ul>
                                <li>
                                        <a href="index.html">Home</a>
                                </li>
                                <li>
                                        <a href="download.html">Download</a>
                                </li>
                        </ul>
                </div>
        </xsl:template>
        <xsl:template name="footer">
                <div id="footer"> <p>SOD is made possible by the <a href="http://www.sc.edu">University
                                of South Carolina's</a>
                        <a href="http://www.geol.sc.edu"> Department of Geological Sciences</a> and
                        the <a href="http://www.iris.edu">IRIS Consortium</a>.</p> </div>
        </xsl:template>
</xsl:stylesheet>
