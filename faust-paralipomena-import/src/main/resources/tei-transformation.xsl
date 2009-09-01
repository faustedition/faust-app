<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:output indent="yes" method="xml" />

	<xsl:template match="/">
		<TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:svg="http://www.w3.org/2000/svg">
			<xsl:apply-templates />
		</TEI>
	</xsl:template>

	<xsl:template match="metadaten" />
	<xsl:template match="kommentar" />

	<xsl:template match="paralipomenon">
		<tei:text>
			<xsl:apply-templates />
		</tei:text>
	</xsl:template>

	<xsl:template match="b">
		<tei:hi rend="bold">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>

	<xsl:template match="br">
		<tei:lb />
	</xsl:template>

	<xsl:template match="div">
		<xsl:element name="tei:div">
			<xsl:attribute name="type">
				<xsl:value-of select="@type" />
			</xsl:attribute>
			<xsl:attribute name="n">
				<xsl:value-of select="@type" />
			</xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<xsl:template match="hr">
		<svg:line />
	</xsl:template>

	<xsl:template match="i">
		<tei:hi rend="italic">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>

	<xsl:template match="img">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="p">
		<tei:p>
			<xsl:apply-templates />
		</tei:p>
	</xsl:template>

	<xsl:template match="strike">
		<tei:hi rend="strikethrough">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>

	<xsl:template match="sub">
		<tei:hi rend="sub">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>

	<xsl:template match="sup">
		<tei:hi rend="sup">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>

	<xsl:template match="u">
		<tei:hi rend="underline">
			<xsl:apply-templates />
		</tei:hi>
	</xsl:template>
</xsl:stylesheet>