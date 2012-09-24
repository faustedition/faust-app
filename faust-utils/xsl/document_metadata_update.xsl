<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:ge="http://www.tei-c.org/ns/geneticEditions" xmlns:svg="http://www.w3.org/2000/svg"
    xmlns:f="http://www.faustedition.net/ns" xmlns="http://www.faustedition.net/ns"
    xpath-default-namespace="http://www.faustedition.net/ns" version="2.0"
    exclude-result-prefixes="xs xsi xd ge svg ">

    <xsl:output indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="f">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- archival document -->

    <xsl:template match="materialUnit[@type='archival_unit']">
        <archivalDocument>
            <xsl:attribute name="xsi:schemaLocation"
                select="'http://www.faustedition.net/ns https://faustedition.uni-wuerzburg.de/xml/schema/metadata.xsd'"/>
            <xsl:apply-templates select="@*|node()"/>
        </archivalDocument>
    </xsl:template>

    <xsl:template match="@type[.='archival_unit']"/>

    <xsl:template match="materialUnit[@type='archival_unit']/@transcript"/>

    <!-- textual transcript -->

    <xsl:template match="materialUnit[@type='archival_unit']/metadata" xml:space="preserve">
        <metadata>
            <textTranscript><xsl:attribute name="uri"><xsl:value-of select="../@transcript"/></xsl:attribute></textTranscript>
             <xsl:apply-templates select="@*|node()"/>
        </metadata>
    </xsl:template>

    <!-- pages -->
    <xsl:template match="materialUnit[@type='page']">
        <docTranscript>
            <xsl:attribute name="uri">
                <xsl:value-of select="@transcript"/>
            </xsl:attribute>
        </docTranscript>
    </xsl:template>


    <!-- top level properties -->

    <xsl:template match="archive">
        <repository>
            <xsl:apply-templates select="@*|node()"/>
        </repository>
    </xsl:template>

    <xsl:template match="callnumber">
        <idno type="gsa_old">
            <xsl:apply-templates select="@*|node()"/>
        </idno>
    </xsl:template>

    <xsl:template match="waId">
        <xsl:if test="*">
            <idno type="wa_faust">
                <xsl:apply-templates select="@*|node()"/>
            </idno>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>
