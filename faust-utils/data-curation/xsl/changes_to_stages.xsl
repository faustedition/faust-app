<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:f="http://www.faustedition.net/ns"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:ge="http://www.tei-c.org/ns/geneticEditions"
    exclude-result-prefixes="xs tei">

    <xsl:output indent="no" method="xml" omit-xml-declaration="yes"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="ge:stageHist">
        <ge:stageNotes>
            <xsl:apply-templates select="@*|node()"/>
        </ge:stageNotes>
    </xsl:template>

    <xsl:template match="ge:stageHist/tei:change">
        <ge:stageNote>
            <xsl:apply-templates select="@*|node()"/>
        </ge:stageNote>
    </xsl:template>
    
    <xsl:template match="@f:changeRef">
        <xsl:attribute name="ge:stage">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    
</xsl:stylesheet>
