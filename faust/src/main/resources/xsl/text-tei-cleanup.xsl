<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0">
	<xsl:output indent="no" />

	<xsl:template match="//tei:text">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="tei:stage">
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates />
			<xsl:choose>
				<xsl:when test="@rend = 'inline'">
					<xsl:text>&#xa0;</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>&#10;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>

	<xsl:template match="tei:l|tei:lg|tei:speaker|tei:sp|tei:fw|tei:head|tei:div">
		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates />
			<xsl:text>&#10;</xsl:text>
		</xsl:element>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:choose>
			<xsl:when test="self::text()">
				<xsl:variable name="normalized" select="normalize-space(.)" />
				<xsl:if test="string-length($normalized) > 0">
					<xsl:value-of select="." />
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>