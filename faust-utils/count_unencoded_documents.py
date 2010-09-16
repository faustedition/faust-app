#!/usr/bin/env python
#
# Report on the transcription status as specified under
#
# https://faustedition.uni-wuerzburg.de/wiki/index.php/Stand_der_Transkription
#

import sys

import lxml.etree

import faust

# XPath expression for extracting the revision history from TEI documents
ge_doc_xp = faust.xpath("normalize-space(//ge:document)")

# XPath expression for extracting the revision history from TEI documents
change_xp = faust.xpath("//tei:teiHeader//tei:revisionDesc/tei:change")

# iterate over all TEI documents
for xml_file in faust.xml_files():
	status = None
	try:
		if faust.is_tei_document(xml_file):
			xml = lxml.etree.parse(xml_file)
			if len(ge_doc_xp(xml).strip()) == 0: continue
			
			encoded = False
			for change in change_xp(xml):
				change_str = lxml.etree.tostring(change).lower()
				if "encoded" in change_str: encoded = True
			if not encoded:
				print faust.relative_path(xml_file)
				
	except IOError:
		sys.stderr.write("I/O error while extracting status from " + xml_file + "\n")