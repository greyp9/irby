<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0'
                xmlns:xed='urn:xed:xed'
                xmlns:xsd='http://www.w3.org/2001/XMLSchema'
                xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

    <xsl:output method='xml' encoding='UTF-8' indent='yes'/>

    <xsl:preserve-space elements='*'/>

    <xsl:template match='@*|node()'>
        <xsl:copy>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/xsd:schema/xsd:complexType[@name="jobCommandType"]/xsd:attribute[@name="command"]'>
        <xsl:copy>
            <xsl:attribute name='xed:size'>132</xsl:attribute>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/xsd:schema/xsd:complexType[@name="principalType"]/xsd:attribute[@name="credential"]'>
        <xsl:copy>
            <xsl:attribute name='xed:hideInTable'>true</xsl:attribute>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/xsd:schema/xsd:complexType[@name="https11Type" or @name="proxysType"]/xsd:attribute[
    @name="keyStorePass" or @name="clientTrustPass"]'>
        <xsl:copy>
            <xsl:attribute name='xed:transform'>AES/GCM/NoPadding</xsl:attribute>
            <xsl:attribute name='xed:parameterSpec'>GCMParameterSpec</xsl:attribute>
            <xsl:attribute name='xed:hideInTable'>true</xsl:attribute>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match='/xsd:schema/xsd:complexType[@name="jobHttpType"]/xsd:attribute[@name="certificate"]'>
        <xsl:copy>
            <xsl:attribute name='xed:rows'>12</xsl:attribute>
            <xsl:attribute name='xed:cols'>132</xsl:attribute>
            <xsl:attribute name='xed:hideInTable'>true</xsl:attribute>
            <xsl:apply-templates select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
