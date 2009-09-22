<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="tei">
	<xsl:template match="/tei:text">
		<div class="tei-text" style="font-family: tahoma">
			<xsl:apply-templates />
		</div>
	</xsl:template>

	<xsl:template match="tei:lb[ancestor::tei:stage|ancestor::tei:l|ancestor::tei:p]">
		<br />
	</xsl:template>

	<xsl:template match="tei:sp|tei:head">
		<div style="margin: 2em 0">
			<xsl:apply-templates />
		</div>
	</xsl:template>

	<xsl:template match="tei:speaker">
		<p class="secondary-color">
			<xsl:apply-templates />
		</p>
	</xsl:template>
	<xsl:template match="tei:stage">
		<div class="small-caps" style="margin: 1em 0">
			<xsl:apply-templates />
		</div>
	</xsl:template>

	<xsl:template match="tei:p|tei:l">
		<p>
			<xsl:apply-templates />
		</p>
	</xsl:template>


	<xsl:template match="tei:hi[@rend='italic']">
		<span style="font-style: italic">
			<xsl:apply-templates />
		</span>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='underline']">
		<span style="text-decoration: underline">
			<xsl:apply-templates />
		</span>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='sub']">
		<sub>
			<xsl:apply-templates />
		</sub>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='sup']">
		<sup>
			<xsl:apply-templates />
		</sup>
	</xsl:template>

	<xsl:template match="tei:add">
		<span style="color: #6f6">
			<xsl:apply-templates />
		</span>
	</xsl:template>

	<xsl:template match="tei:del">
		<span style="color: #f66; text-decoration: line-through">
			<xsl:apply-templates />
		</span>
	</xsl:template>

	<xsl:template match="tei:expan" />
	<xsl:template match="tei:corr" />
	<xsl:template match="tei:reg" />
</xsl:stylesheet>