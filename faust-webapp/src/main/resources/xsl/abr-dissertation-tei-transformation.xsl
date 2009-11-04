<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0" xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:output indent="yes" method="xml" />

	<xsl:template match="/">
		<TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:svg="http://www.w3.org/2000/svg">
			<xsl:apply-templates />
		</TEI>
	</xsl:template>

	<xsl:template match="*[local-name() = 'metadaten']" />
	<xsl:template match="*[local-name() = 'kommentar']" />

	<xsl:template match="*[local-name() = 'paralipomenon']">
		<text>
			<xsl:apply-templates />
		</text>
	</xsl:template>

	<xsl:template match="*[local-name() = 'b']">
		<hi rend="bold">
			<xsl:apply-templates />
		</hi>
	</xsl:template>

	<xsl:template match="*[local-name() = 'br']">
		<lb />
	</xsl:template>

	<xsl:template match="*[local-name() = 'div']">
		<xsl:element name="div">
			<xsl:attribute name="type">
				<xsl:value-of select="@type" />
			</xsl:attribute>
			<xsl:attribute name="n">
				<xsl:value-of select="@n" />
			</xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<xsl:template match="*[local-name() = 'hr']">
		<svg:line />
	</xsl:template>

	<xsl:template match="*[local-name() = 'i']">
		<hi rend="italic">
			<xsl:apply-templates />
		</hi>
	</xsl:template>

	<xsl:template match="*[local-name() = 'img']">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="*[local-name() = 'p']">
		<p>
			<xsl:apply-templates />
		</p>
	</xsl:template>

	<xsl:template match="*[local-name() = 'strike']">
		<hi rend="strikethrough">
			<xsl:apply-templates />
		</hi>
	</xsl:template>

	<xsl:template match="*[local-name() = 'sub']">
		<hi rend="sub">
			<xsl:apply-templates />
		</hi>
	</xsl:template>

	<xsl:template match="*[local-name() = 'sup']">
		<hi rend="sup">
			<xsl:apply-templates />
		</hi>
	</xsl:template>

	<xsl:template match="*[local-name() = 'u']">
		<hi rend="underline">
			<xsl:apply-templates />
		</hi>
	</xsl:template>
</xsl:stylesheet>