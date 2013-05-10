<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:include href="copyAll.xslt"/>
    <xsl:template match="networkCode">
        <channelAND><xsl:text>&#xa;            </xsl:text>
            <networkCode>II</networkCode><xsl:text>&#xa;            </xsl:text>
            <bandCode>B</bandCode><xsl:text>&#xa;        </xsl:text>
        </channelAND>
    </xsl:template>
</xsl:stylesheet>
