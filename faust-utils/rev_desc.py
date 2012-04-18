#!/usr/bin/env python
# coding=UTF-8
#
# Add a revision description change to an xml file represented by an ElementTree

import lxml.etree
import faust
import datetime

revdesc_xp = faust.xpath("//tei:teiHeader/tei:revisionDesc")
header_xp = faust.xpath("//tei:teiHeader")

def add_change (xml, who, content, when = datetime.date.today().isoformat()):
	'''Adds a change element to the revisionDesc in the TEI header'''

	header = header_xp(xml)

	if not header:
		raise ValueError("No TEI header present")

	# if there is no tei:revisionDesc element, insert one
	if not revdesc_xp(xml):
		rev_desc_element = lxml.etree.Element(faust.ns("tei") + "revisionDesc")
		# revisionDesc always goes to the end of the header
		header[0].append(rev_desc_element)


	# build change element
	attribs = {"when" : when,
		   "who" : who }
	change = lxml.etree.Element(faust.ns("tei") + "change", attribs)
	change.text = content

	revdesc_xp(xml)[0].append(change)




