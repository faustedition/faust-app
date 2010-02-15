<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="tei">
	<xsl:output omit-xml-declaration="yes" />

	<xsl:template match="/tei:text">
		<fo:root>
			<fo:layout-master-set>
				<fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm">
					<fo:region-body region-name="xsl-region-body" margin="2cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="A4">
				<fo:flow flow-name="xsl-region-body" font-family="Times" font-size="14pt">
					<xsl:apply-templates />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:template match="tei:lb[ancestor::tei:stage|ancestor::tei:l|ancestor::tei:p]">
		<fo:block />
	</xsl:template>

	<xsl:template match="tei:sp|tei:head">
		<fo:block space-after="0.5cm" space-before="1cm">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>

	<xsl:template match="tei:speaker">
		<fo:block font-style="italic">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>
	<xsl:template match="tei:stage">
		<fo:block font-variant="small-caps" space-before="1cm" space-after="0.5cm">
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>

	<xsl:template match="tei:p|tei:l">
		<fo:block>
			<xsl:apply-templates />
		</fo:block>
	</xsl:template>


	<xsl:template match="tei:hi[@rend='italic']">
		<fo:inline font-style="italic">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='underline']">
		<fo:inline text-decoration="underline">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='sub']">
		<fo:inline baseline-shift="sub" font-size="smaller">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:hi[@rend='sup']">
		<fo:inline baseline-shift="sup" font-size="smaller">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:add">
		<fo:inline color="green">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:del">
		<fo:inline color="red" text-decoration="line-through">
			<xsl:apply-templates />
		</fo:inline>
	</xsl:template>

	<xsl:template match="tei:expan" />
	<xsl:template match="tei:corr" />
	<xsl:template match="tei:reg" />
</xsl:stylesheet>