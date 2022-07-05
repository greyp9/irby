<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0'
                xmlns:irby='urn:irby:app'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method='xml' encoding='UTF-8' indent='yes'/>

    <xsl:preserve-space elements='*'/>

    <xsl:param name="port"/>
    <xsl:param name="keyStoreType"/>
    <xsl:param name="keyStoreFile"/>
    <xsl:param name="keyStorePass"/>

    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/irby:application/irby:https11[@name="https-default"]/@enabled'>
        <xsl:choose>
            <xsl:when test="string-length($port) &gt; 0">
                <xsl:attribute name='enabled'>true</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:https11[@name="https-default"]/@port'>
        <xsl:choose>
            <xsl:when test="string-length($port) &gt; 0">
                <xsl:attribute name='port'>
                    <xsl:value-of select="$port"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:https11[@name="https-default"]/@keyStoreType'>
        <xsl:choose>
            <xsl:when test="string-length($keyStoreType) &gt; 0">
                <xsl:attribute name='keyStoreType'>
                    <xsl:value-of select="$keyStoreType"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:https11[@name="https-default"]/@keyStoreFile'>
        <xsl:choose>
            <xsl:when test="string-length($keyStoreFile) &gt; 0">
                <xsl:attribute name='keyStoreFile'>
                    <xsl:value-of select="$keyStoreFile"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:https11[@name="https-default"]/@keyStorePass'>
        <xsl:choose>
            <xsl:when test="string-length($keyStorePass) &gt; 0">
                <xsl:attribute name='keyStorePass'>
                    <xsl:value-of select="$keyStorePass"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
