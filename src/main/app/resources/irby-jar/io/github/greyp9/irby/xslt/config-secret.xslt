<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0'
                xmlns:irby='urn:irby:app'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method='xml' encoding='UTF-8' indent='yes'/>

    <xsl:preserve-space elements='*'/>

    <xsl:param name="secretFile"/>

    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/irby:application/@secret'>
        <xsl:choose>
            <xsl:when test="string-length($secretFile) &gt; 0">
                <xsl:attribute name="secret">
                    <xsl:value-of select="$secretFile"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
