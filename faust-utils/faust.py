#!/usr/bin/env python
#
# Helper functions and routines common to Faust related tasks
#

import ConfigParser
import StringIO
import email.mime.text
import os
import os.path
import smtplib

import lxml.etree

config = ConfigParser.ConfigParser()
config.read(['faust.ini', "local.ini"])

xml_dir = config.get("xml", "dir")
facs_dir = os.path.join(config.get("facsimile", "dir"))
faust_scheme = "faust"
report_sender = "Faust-Edition <noreply@faustedition.net>"
report_recipients = [
	"Katrin Henzel <henzel@faustedition.de>",
	"Gerrit Bruening <bruening@faustedition.de>",
	"Dietmar Pravida <pravida@faustedition.de>",
	"Moritz Wissenbach <m.wissenbach@gmx.de>",
	"Gregor Middell <gregor@middell.net>",
]

def relative_path(xml_file):
	"""Returns the path of the given XML file relative to the base directory"""
	prefix = xml_dir + "/"
	if xml_file.startswith(prefix):
		return xml_file[len(prefix):]
	else:
		return xml_file

def absolute_path(xml_file):
	"""Returns the absolute path of the XML file specified relative to the base directory"""
	return "/".join((xml_dir, xml_file))
		 
def xml_files():
	"""Returns paths of all XML documents in the edition"""
	xml_files = []
	for root, dirs, files in os.walk(xml_dir):		
		for f in files: 
			if f.endswith(".xml"): xml_files.append(os.path.join(root, f))
	xml_files.sort()
	return xml_files

def files_in(prefix):
	return [f for f in xml_files() if relative_path(f).startswith(prefix)]

def transcript_files():
	return [f for f in xml_files() if relative_path(f).startswith("transcript/")]

def document_files():
	return [f for f in xml_files() if relative_path(f).startswith("document/")]

def metadata_files():
	return [f for f in xml_files() if relative_path(f).startswith("metadata/")]

def macrogenesis_files():
	return [f for f in xml_files() if relative_path(f).startswith("macrogenesis/")]

def is_tei_document(xml_file):
	"""Determines whether a XML file is a TEI document by checking the namespace of the first element encountered"""
	for event, element in lxml.etree.iterparse(xml_file):
		if element is None: continue
		return element.tag.startswith("{http://www.tei-c.org/ns/1.0}")

def facsimiles():
	"""Returns a list of all faust facsimile URIs"""
	result = []
	tif_dir = os.path.join(facs_dir, "tif/")
	for root, dirs, files in os.walk(tif_dir):
		for f in files:
			path = os.path.join(root,f)
			uri = faust_scheme + "://facsimile/" + path[len(tif_dir) : - len(".tif")]
			result.append(uri)
	return result
	

def send_report(subject, msg):
	if config.getboolean("mail", "enabled"):
		msg = email.mime.text.MIMEText(msg)
		msg["From"] = report_sender
		msg["To"] = ", ".join(report_recipients)
		msg["Subject"] = subject
		
		server = smtplib.SMTP('localhost')
		server.sendmail(report_sender, report_recipients, msg.as_string())
		server.quit()
	else:
		print "From:", report_sender
		print "To:", ", ".join(report_recipients)
		print "Subject:", subject
		print
		print msg
	

tei_serialization_xslt = StringIO.StringIO('''\
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="no" omit-xml-declaration="yes" />
	<!-- <xsl:strip-space elements="*" /> -->
	<xsl:template match="/">
		<xsl:processing-instruction name="oxygen">RNGSchema="%s" type="xml"</xsl:processing-instruction>
		<xsl:apply-templates select="*[position() = last()]"/>
	</xsl:template>
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>''' % config.get("validate", "schema"))
tei_serialize = lxml.etree.XSLT(lxml.etree.parse(tei_serialization_xslt))
	
namespaces = {
	"tei": "http://www.tei-c.org/ns/1.0",
        "ge": "http://www.tei-c.org/ns/geneticEditions",
        "f": "http://www.faustedition.net/ns",
        "svg": "http://www.w3.org/2000/svg",
        #"exist": "http://exist.sourceforge.net/NS/exist",
	"xml": "http://www.w3.org/XML/1998/namespace",
}

ns = lambda prefix: "{" + namespaces[prefix] + "}"

def xpath(expr, node=None):
	if node is None: return lxml.etree.XPath(expr, namespaces=namespaces)
	return node.xpath(expr, namespaces=namespaces)
