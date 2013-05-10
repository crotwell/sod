<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:include href="copyAll.xslt"/>
    <xsl:template match="networkArm">
        <networkArm>Same as network tutorial</networkArm>
    </xsl:template>
    <xsl:template match="eventArm">
        <eventArm>Same as event tutorial</eventArm>
    </xsl:template>
</xsl:stylesheet>
