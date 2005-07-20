<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:include href="copyAll.xslt"/>
    <xsl:template match="originOR">
        <printlineEventProcess>
            <template>From server: $event</template>
        </printlineEventProcess>
        <xsl:call-template name="copy"/>
    </xsl:template>
    <xsl:template match="printlineEventProcess">
        <printlineEventProcess>
            <template>Passed boolean subsetter: $event</template>
        </printlineEventProcess>
    </xsl:template>
</xsl:stylesheet>
