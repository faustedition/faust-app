#!/usr/bin/env python
#
# Search for missing @xml:space

import lxml.etree

import faust

ignored_tags = (
	"app", "back", "body", "choice", "div", "docTitle", "fix", "front", "fw", "g", 
	"group", "lg", "overw", "patch", "sp", "subst", "surface", "text", "titlePage", "titlePart", 
	"used", "zone")
	
ignored_empty_elems = (
	"addSpan", "anchor", "cb", "certainty", "damageSpan", "delSpan", "gap", "grBrace", "grLine", "handShift", 
	"ins", "join", "lb", "pb", "space", "st", "undo", "p")

element_selector_xp = faust.xpath("//*[(ancestor::tei:text or ancestor::ge:document) and not(@xml:space)]")
text_content_xp = faust.xpath("normalize-space()")

candidates = dict()
for xml_file in faust.xml_files():
	try:
		if faust.is_tei_document(xml_file):
			xml = lxml.etree.parse(xml_file)
			
			xml_key = faust.relative_path(xml_file)
			candidates[xml_key] = []
			
			for elem in element_selector_xp(xml):
				if elem.tag.startswith(faust.ns("svg")): continue
				
				local_name = elem.tag[elem.tag.rfind("}") + 1:]
				if local_name in ignored_tags: continue
				
				empty_elem = elem.text is None and len(elem) == 0
				if empty_elem and local_name in ignored_empty_elems: continue
				
				text_content = text_content_xp(elem)
				if empty_elem or (len(text_content) > 0 and len(text_content.strip()) == 0):
					candidates[xml_key].append(lxml.etree.tostring(elem))
	except IOError:
		sys.stderr.write("I/O error while validating " + xml_file + "\n")

for xml_key in candidates:
	elems = candidates[xml_key]
	if len(elems) > 0: print xml_key, "===>", repr(elems)