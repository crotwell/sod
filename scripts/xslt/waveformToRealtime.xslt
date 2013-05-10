<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:include href="copyAll.xslt"/>
    <xsl:template match="eventArm">
        <properties><xsl:text>&#xa;        </xsl:text>
            <maxRetryDelay><xsl:text>&#xa;            </xsl:text>
                <unit>DAY</unit><xsl:text>&#xa;            </xsl:text>
                <value>5</value><xsl:text>&#xa;        </xsl:text>
            </maxRetryDelay><xsl:text>&#xa;    </xsl:text>
        </properties><xsl:text>&#xa;    </xsl:text>
        <xsl:call-template name="copy"/>
    </xsl:template>
    <xsl:template match="someCoverage">
        <availableDataAND><xsl:text>&#xa;            </xsl:text>
            <availableDataOR><xsl:text>&#xa;                </xsl:text>
                <fullCoverage/><xsl:text>&#xa;                </xsl:text>
                <postEventWait><xsl:text>&#xa;                    </xsl:text>
                    <unit>DAY</unit><xsl:text>&#xa;                    </xsl:text>
                    <value>4</value><xsl:text>&#xa;                </xsl:text>
                </postEventWait><xsl:text>&#xa;            </xsl:text>
            </availableDataOR><xsl:text>&#xa;            </xsl:text>
            <someCoverage/><xsl:text>&#xa;        </xsl:text>
        </availableDataAND>
    </xsl:template>
</xsl:stylesheet>
