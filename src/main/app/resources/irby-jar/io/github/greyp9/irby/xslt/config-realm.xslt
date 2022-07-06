<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0'
                xmlns:irby='urn:irby:app'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method='xml' encoding='UTF-8' indent='yes'/>

    <xsl:preserve-space elements='*'/>

    <xsl:param name="realmType"/>
    <xsl:param name="credential"/>
    <xsl:param name="name"/>

    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/irby:application/irby:realm[@name="Irby-Basic"]/@enabled'>
        <xsl:choose>
            <xsl:when test="string-length($realmType) = 0">
                <xsl:copy/>
            </xsl:when>
            <xsl:when test="$realmType = 'Basic'">
                <xsl:attribute name='enabled'>true</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name='enabled'>false</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:realm[@name="Irby-Certificate"]/@enabled'>
        <xsl:choose>
            <xsl:when test="string-length($realmType) = 0">
                <xsl:copy/>
            </xsl:when>
            <xsl:when test="$realmType = 'Certificate'">
                <xsl:attribute name='enabled'>true</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name='enabled'>false</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='/irby:application/irby:realm[@name="Irby-Certificate"]'>
        <xsl:choose>
            <xsl:when test="$realmType = 'Certificate'">
                <xsl:copy>
                    <xsl:apply-templates select='@*'/>  <!-- drop any existing certificate principal -->
                    <xsl:element name="irby:principal">
                        <xsl:attribute name="credential">
                            <xsl:value-of select="$credential"/>
                        </xsl:attribute>
                        <xsl:attribute name="name">
                            <xsl:value-of select="$name"/>
                        </xsl:attribute>
                        <xsl:attribute name="roles">*</xsl:attribute>
                    </xsl:element>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select='@*|node()'/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match='//irby:web-app/@realm'>
        <!-- set the webapp realm to match the enabled realm -->
        <xsl:attribute name="realm">
            <xsl:value-of select="/irby:application/irby:realm[@enabled='true']/@name"/>
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
