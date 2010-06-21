<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:f="http://www.faustedition.net/ns"
	xmlns:ge="http://www.tei-c.org/ns/geneticEditions" xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:strip-space elements="*" />

	<xsl:template match="ge:stageHist">
		<ge:stageNotes>
			<xsl:apply-templates />
		</ge:stageNotes>
	</xsl:template>

	<xsl:template match="tei:change[parent::ge:stageHist]">
		<xsl:element name="ge:stageNote">
			<xsl:for-each select="@*">
				<xsl:copy />
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="svg:line">
		<f:grLine f:orient="horiz" />
	</xsl:template>

	<xsl:template match="tei:div[@type='Blatt, Seite, VS, RS']">
		<div>
			<xsl:comment>Typ: <xsl:value-of select="@n" /></xsl:comment>
			<xsl:apply-templates />
		</div>
	</xsl:template>

	<xsl:template match="tei:div[@type='Keine P-Nummer']">
		<div><xsl:apply-templates/></div>
	</xsl:template>
	
	<xsl:template match="@f:changeRef">
		<xsl:attribute name="ge:stage"><xsl:value-of select="string(.)" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>