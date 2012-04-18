#!/usr/bin/env python
#
# Updates the all TEI headers based on a template
#

import copy
import sys

import lxml.etree

import faust

# XPath expressions for extraction of templated header contents
handNotes_xp = faust.xpath("//tei:teiHeader/tei:profileDesc/tei:handNotes")
charDecl_xp = faust.xpath("//tei:teiHeader/tei:encodingDesc/tei:charDecl")

# Get the template and parse it
tei_template = faust.absolute_path("template/tei.xml")
template = lxml.etree.parse(tei_template)

# extract relevant header fragments from template
template_hand_notes = handNotes_xp(template)[0]
template_char_decl = charDecl_xp(template)[0]


def replace(node, with_node):
	'''Replaces a node with a deep copy of a node (from another document)'''
	node.getparent().replace(node, copy.deepcopy(with_node))

# iterate over TEI files (excluding the template)
for xml_file in faust.xml_files():
	try:
		if (xml_file != tei_template) and faust.is_tei_document(xml_file):
			xml = lxml.etree.parse(xml_file)
	
			# replace header fragments
			for hand_notes in handNotes_xp(xml): replace(hand_notes, template_hand_notes)
			for char_decl in charDecl_xp(xml): replace(char_decl, template_char_decl)
	
			# write back updated document
			faust.tei_serialize(xml).write(xml_file, encoding="UTF-8")
	except IOError:
		sys.stderr.write("I/O error while updating " + xml_file + "\n")
	except lxml.etree.XMLSyntaxError:
		sys.stderr.write("XML syntax error while updating " + xml_file + "\n")
