<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:f="http://www.faustedition.net/ns"
	xmlns:ge="http://www.tei-c.org/ns/geneticEditions" xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:strip-space elements="*" />

	<xsl:template match="tei:text">
		<surface><xsl:apply-templates /></surface>
	</xsl:template>

	<xsl:template match="tei:body">
		<xsl:choose>
			<xsl:when test=".//tei:div[@type='zone']"><xsl:apply-templates /></xsl:when>
			<xsl:otherwise><zone><xsl:apply-templates /></zone></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="tei:lg|tei:sp">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="tei:div[@type='zone']">
		<zone><xsl:apply-templates/></zone>
	</xsl:template>
	
	<xsl:template match="tei:div">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="tei:speaker">
		<xsl:element name="ge:line">
			<xsl:choose>
				<xsl:when test="contains(string(@rend), 'underline') or contains(string(@rend), 'underlined')"><hi rend="underline"><xsl:for-each select="@rend"><xsl:copy/></xsl:for-each><xsl:apply-templates/></hi></xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="@rend"><xsl:copy/></xsl:for-each>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>	
	
	<xsl:template match="tei:l|tei:p|tei:head|tei:stage|tei:note">
		<xsl:element name="ge:line">
			<xsl:for-each select="@rend|@place">
				<xsl:copy/>
			</xsl:for-each>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="tei:subst[@type='overwrite']">
		<xsl:element name="f:overw">
			<xsl:for-each select="@*"><xsl:copy/></xsl:for-each>
			<xsl:element name="f:under">
				<xsl:for-each select="tei:del/@*">
					<xsl:copy/>
				</xsl:for-each>
				<xsl:apply-templates select="tei:del/*"/>
			</xsl:element>
			<xsl:element name="f:over">
				<xsl:for-each select="tei:add/@*">
					<xsl:copy/>
				</xsl:for-each>
				<xsl:apply-templates select="tei:add/*"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="tei:del">
		<xsl:element name="f:st">
			<xsl:for-each select="@*">
				<xsl:copy/>
			</xsl:for-each>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="tei:fix|tei:repetition">
		<xsl:element name="ge:rewrite">
			<xsl:for-each select="@*">
				<xsl:copy/>
			</xsl:for-each>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>