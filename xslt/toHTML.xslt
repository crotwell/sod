<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="xmlverbatim.xsl"/>
    <xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.01//EN"/>
    <xsl:template match="/">
        <html>
            <head>
                <title>XML source view</title>
            </head>
            <body>
                <pre>
                    <xsl:text>&lt;?xml version="1.0"?>&#xa;</xsl:text>
                    <xsl:apply-templates select="." mode="xmlverb"/>
                </pre>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
