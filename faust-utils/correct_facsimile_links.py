#!/usr/bin/env python
# coding=UTF-8
#
# Correct the links to facsimile files
#
import faust
import transform
import lxml
import os
import sys
import rev_desc

doc_template = lxml.etree.parse(os.path.join(faust.xml_dir, "template", "tei.xml"))
graphic_xp = faust.xpath ("//tei:facsimile/tei:graphic")
header_xp = faust.xpath ("/tei:TEI/tei:teiHeader")
valid_graphic_uris = faust.facsimiles()

def xml_names_from_facsimiles():
	prefix_length = len(faust.faust_scheme + "://facsimile/")
	def to_xml_path (facs_uri):
		stripped = facs_uri[prefix_length:]
		return os.path.join(faust.xml_dir, "transcript" , stripped + ".xml")
	return map (to_xml_path, faust.facsimiles())

def facs_uri_from_xml(path):
	stripped = path[len(faust.xml_dir + "/facsimile/") : - len(".xml")]
	return faust.faust_scheme + "://facsimile" + stripped 
	
def make_xml_templates():
	xml_templates = xml_names_from_facsimiles()
	# check if all directories exist
	for path in xml_templates:
		dirname = os.path.dirname (path)
		if not os.path.exists(dirname):
			raise Exception("Directory doesn't exist: " + dirname)
	for path in xml_templates:
		if os.path.exists (path):
			print "  exists: ", path
		else:
			#don't generate new GSA files, they should be ok.
			if not "/gsa/" in path and not "/GSA/" in path:
				make_template(path)

def make_template(path):
	print "creating: " , path
	faust.tei_serialize(doc_template).write(path, encoding='UTF-8')
	

def append_facsimile_element(xml):
	print "   appending <facsimile/>"
	facsimile = lxml.etree.Element(faust.ns("tei") + "facsimile")
	graphic = lxml.etree.Element(faust.ns("tei") + "graphic")
	graphic.attrib["url"] = ""
	facsimile.append(graphic)
	# attach behind teiHeader
	header_xp(xml)[0].addnext(facsimile)
	

def correct_uri(old, brutal, xml_file):
	''' brutal means try to replace by the corresponding image path'''
	if old in valid_graphic_uris:
		return old
	new = old.replace("/GSA/", "/gsa/")
	if new in valid_graphic_uris:
		print "   lowercase gsa"
		return new

	if brutal:
		new = facs_uri_from_xml(xml_file)
		if new in valid_graphic_uris:
			print "   facsimile by xml name"
			return new

	print "   WARNING: Could not correct "
#	pdb.set_trace()
	return old

def correct_graphic_uris():
	# take into account old GSA files
	files = [f for f in faust.transcript_files() if '/gsa/' in f]
	files.extend(xml_names_from_facsimiles())
	for f in files:
		rewrite_file = False
	
		try:
			xml = lxml.etree.parse(f)
		except IOError:
			# these should only be GSA files
			print "(", f, " doesn't exist)"
			continue
		print f
		graphics = graphic_xp(xml)

		if len(graphics) == 0:
			append_facsimile_element(xml)
			# find the newly appended element
			graphics = graphic_xp(xml)

		brutal = False
		if len(graphics) == 1:
			brutal = True

		for graphic in graphics:
			old = graphic.attrib["url"]
			new = correct_uri(old, brutal, f)
			graphic.attrib["url"] = new
			if new != old:
				print "   correcting: ", old, " -> ", new
				rewrite_file = True
		if rewrite_file:
			rev_desc.add_change(xml, "system", "facsimile_adapted")
			print "   writing"
			faust.tei_serialize(xml).write(f, encoding='UTF-8')
		else:
			print "   not writing"

if __name__ == "__main__":
	make_xml_templates()
	correct_graphic_uris()

