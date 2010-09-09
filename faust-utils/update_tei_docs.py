#!/usr/bin/env python

import copy
import sys

import lxml.etree

import faust

handNotes_xp = faust.xpath("//tei:teiHeader/tei:profileDesc/tei:handNotes")
charDecl_xp = faust.xpath("//tei:teiHeader/tei:encodingDesc/tei:charDecl")

tei_template = faust.absolute_path("templates/tei.xml")
template = lxml.etree.parse(tei_template)

template_hand_notes = handNotes_xp(template)[0]
template_char_decl = charDecl_xp(template)[0]

schema_data = 'RNGSchema="%s" type="xml"' % faust.config.get("validate", "schema")

def replace(node, with_node): node.getparent().replace(node, copy.deepcopy(with_node))

for xml_file in faust.xml_files():
	if (xml_file != tei_template) and faust.is_tei_document(xml_file):
		xml = lxml.etree.parse(xml_file)
		
		for hand_notes in handNotes_xp(xml): replace(hand_notes, template_hand_notes)
		for char_decl in charDecl_xp(xml): replace(char_decl, template_char_decl)
		
		faust.tei_serialize(xml).write(xml_file, encoding="UTF-8")
