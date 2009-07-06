<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0"
	xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:output doctype-system="http://www.faustedition.net/schema/v1/faust-tei.dtd" indent="yes" method="xml" encoding="UTF-8" />
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>