<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:f="http://www.faustedition.net/ns"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:ge="http://www.tei-c.org/ns/geneticEditions"
    exclude-result-prefixes="xs tei">

    <xsl:output indent="no" method="xml" omit-xml-declaration="no"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="//tei:text[not(.//text() or //tei:div[@type='template' or .//comment()])]">
    </xsl:template>
    
</xsl:stylesheet>
