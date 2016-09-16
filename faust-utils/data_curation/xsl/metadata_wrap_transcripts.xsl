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

    <xsl:template match="archivalDocument/docTranscript">
        <disjunctLeaf type="import">
            <page>
                <metadata>
                    <xsl:copy exclude-result-prefixes="f">
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </metadata>
            </page>
            <page></page>
        </disjunctLeaf>
        
    </xsl:template>
</xsl:stylesheet>
