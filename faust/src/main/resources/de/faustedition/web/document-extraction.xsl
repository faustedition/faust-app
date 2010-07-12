<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:f="http://www.faustedition.net/ns" xmlns:ge="http://www.tei-c.org/ns/geneticEditions"
	xmlns="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="tei">
	<xsl:output omit-xml-declaration="yes" indent="no" />

	<xsl:template match="/">
		<xsl:apply-templates select="//ge:document" />
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>