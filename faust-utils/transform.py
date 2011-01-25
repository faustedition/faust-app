#!/usr/bin/env python
#
# Convenience methods for executing transforms on all files
#

import sys
import lxml.etree
import faust

def transform_all(files, transform):
	for f in files:
		transform(f)

def tei_transform (tei_file, transform_etree):
	try:
		if not faust.is_tei_document(tei_file):
			sys.stderr.write("Not a TEI file: " + file + "\n")
			return
		xml = lxml.etree.parse(tei_file)
		result = transform_etree(xml)
		faust.tei_serialize(result).write(tei_file, encoding="UTF-8")
	except IOError:
		sys.stderr.write("I/O error while transforming " + tei_file + "\n")
	except lxml.etree.XMLSyntaxError:
		sys.stderr.write("XML syntax error while transforming " + tei_file + "\n")


# ===== applications =====

def transform_stages_to_changes():
	xslt_trans = lxml.etree.XSLT(lxml.etree.parse("xsl/changes_to_stages.xsl"))
	changes_to_stages = lambda t: tei_transform(t, xslt_trans)
	transform_all(faust.transcript_files(), changes_to_stages)

transform_stages_to_changes()
