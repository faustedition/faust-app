#!/usr/bin/env python
#
# Convert old text-based encodings to document oriented ones
#

import copy
import re

import lxml.etree

import faust

def to_convert():
	text_content_xp = faust.xpath("normalize-space(//tei:text)")
	ge_document_content_xp = faust.xpath("normalize-space(//ge:document)")
	has_text_in = lambda xp, xml: (len(" ".join(xp(xml)).strip()) > 0)
	
	to_convert = list()
	for xml_file in faust.xml_files():
		path = faust.relative_path(xml_file).split("/")
		if path[0] != "transcription": continue	
		file_name = path[-1]
		if file_name[:-len(".xml")] == path[-2]: continue
		if int(re.search(r'[0-9]+', file_name).group(0)) == 1: continue
		if not faust.is_tei_document(xml_file):	continue
	
		xml = lxml.etree.parse(xml_file)
		if has_text_in(text_content_xp, xml) and not has_text_in(ge_document_content_xp, xml):
			to_convert.append(xml_file)
	return to_convert

def convert():
	tei_text_xp = faust.xpath("//tei:text")
	xml_id_cnt = 0
	
	for xml_file in static_to_convert():
		print xml_file
				
		xml = lxml.etree.parse(xml_file)
		tei_text = tei_text_xp(xml)[0]
		
		# prepare <ge:document/> context
		root = lxml.etree.Element(faust.ns("ge") + "document", nsmap=faust.namespaces)
		root.set("type", "converted")
		root = lxml.etree.SubElement(root, faust.ns("tei") + "surface")
		surface = root
		if len(faust.xpath(".//tei:div[@type='zone']", tei_text)) == 0:
			root = lxml.etree.SubElement(root, faust.ns("tei") + "zone")
		for body_content in faust.xpath("tei:body/*", tei_text):
			root.append(copy.deepcopy(body_content))

		# let <add/>/<del/> inherit @hand from <subst/>/<restore/>
		for container_with_hand in faust.xpath(".//tei:subst[@hand]|./tei:restore[@hand]", root):
			hand = container_with_hand.get("hand")
			for add_xml in faust.xpath("./tei:add[count(@hand) = 0]", container_with_hand):
				add_xml.set("hand", hand)
			for del_xml in faust.xpath("./tei:del[count(@hand) = 0]", container_with_hand):
				del_xml.set("hand", hand)
			del container_with_hand.attrib["hand"]
						
		# convert @hand into <handShift/>
		for hand_annotated in faust.xpath(".//*[@hand]", root):
			if hand_annotated.tag not in (faust.ns("tei") + "add", faust.ns("tei") + "fw"): continue
			
			handShifts = faust.xpath("./preceding::tei:handShift", hand_annotated)
			last_hand = (len(handShifts) > 0) and handShifts[-1].get("new") or "#i_have_no_idea"
			
			# start of new hand
			hs = lxml.etree.Element(faust.ns("tei") + "handShift")
			hs.set("new", hand_annotated.get("hand"))
			hs.tail = hand_annotated.text
			
			hand_annotated.text = None
			hand_annotated.insert(0, hs)
			
			
			# reset to last hand
			hs = lxml.etree.Element(faust.ns("tei") + "handShift")
			hs.set("new", last_hand)
			hand_annotated.append(hs)
			
			del hand_annotated.attrib["hand"]
				
		# convert <div/> with @type == "zone"
		for div in root.iter(faust.ns("tei") + "div"):
			if "zone" == div.get("type", ""):
				div.tag = faust.ns("tei") + "zone"
				del div.attrib["type"]

		# convert overwritten parts
		for subst in root.iter(faust.ns("tei") + "subst"):
			if subst.get("type", "") == "overwrite":
				subst.tag = faust.ns("f") + "overw"
				del subst.attrib["type"]
				for del_xml in subst.findall(faust.ns("tei") + "del"):
					del_xml.tag = faust.ns("f") + "under"
				for add in subst.findall(faust.ns("tei") + "add"):
					add.tag = faust.ns("f") + "over"


		# throw away text structure tagging
		lxml.etree.strip_tags(root,\
			faust.ns("tei") + "div", faust.ns("tei") + "lg",\
			faust.ns("tei") + "sp", faust.ns("tei") + "subst")

		# create simple lines
		for line_element in ("speaker", "l", "p", "stage", "note", "head", "ab"):
			for le in root.iter(faust.ns("tei") + line_element):
				le.tag = faust.ns("ge") + "line"
		
		# turn deletions into <f:st/> by default
		for del_xml in root.iter(faust.ns("tei") + "del"):
			del_xml.tag = faust.ns("f") + "st"
			del_type = del_xml.get("type", "")
			if del_type == "strikethrough" or del_type == "strikedthrough": 
				del del_xml.attrib["type"]
			
		# rename tags for fixations
		for rewrite_tag in ("fix", "repetition"):
			for rewrite in root.iter(faust.ns("tei") + rewrite_tag):
				rewrite.tag = faust.ns("ge") + "rewrite"

		# convert inline <lb/> to <ge:line/>
		for lb in list(root.iter(faust.ns("tei") + "lb")):
			parent = lb.getparent()
			if parent.tag != (faust.ns("ge") + "line"): continue

			lb.addprevious(lxml.etree.Comment("lbconversion"))
			lb.tag = faust.ns("ge") + "line"
			lb.text = lb.tail
			lb.tail = None
			sibling = lb.getnext()
			while sibling is not None:
				next_sibling = sibling.getnext()
				parent.remove(sibling)
				lb.append(sibling)
				sibling = next_sibling			
			parent.remove(lb)
			parent.addnext(lb)

		# detach marginal notes
		for margin in list(faust.xpath(".//*[@place]", root)):
			place = margin.get("place")
			if place not in ("margin",\
			 	"top", "top-left", "topleft", "top-right", "topright",\
				"bottom", "bottom-left", "bottomleft", "bottom-right", "bottomright"):
				continue

			parent = margin.getparent()
			
			margin_zone = lxml.etree.Element(faust.ns("tei") + "zone")
			if place.startswith("top"):
				surface.insert(0, margin_zone)
			else:
				surface.append(margin_zone)
				
			margin_parent = margin_zone
			if margin.tag != faust.ns("ge") + "line":
				margin_parent = lxml.etree.SubElement(margin_parent, faust.ns("ge") + "line")
				
			if parent.tag == (faust.ns("ge") + "line"):
				line_id = parent.get(faust.ns("xml") + "id", None)
				if line_id is None:
					xml_id_cnt += 1
					line_id = "line_" + str(xml_id_cnt)
					parent.set(faust.ns("xml") + "id", line_id)
				margin_zone.set(faust.ns("f") + "top", "#" + line_id)
			
			parent.remove(margin)
			margin_parent.append(margin)
			
		# remove <lb/>s, which are located in zones after conversion
		for lb in list(root.iter(faust.ns("tei") + "lb")):
			parent = lb.getparent()
			if parent.tag == (faust.ns("tei") + "zone"):
				parent.remove(lb)

		print lxml.etree.tostring(root, pretty_print=True)

def static_to_convert():
	return [
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389872/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389872/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389872/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389872/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389877/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389877/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389890/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389890/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389890/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389892/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389892/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/389992/0008.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0006.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0007.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0008.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0009.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0010.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0011.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0012.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0013.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0014.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0015.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0016.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0017.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0018.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0019.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0020.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0021.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0022.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0023.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0024.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0025.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0026.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0027.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0028.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0029.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0030.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0031.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0032.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0033.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0034.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0035.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0036.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0037.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0038.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0039.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0040.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0041.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0042.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0043.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0044.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0045.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0046.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0047.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0048.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0049.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0050.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0051.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0052.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0053.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0054.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0055.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0056.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0057.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0058.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0059.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0060.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0061.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0062.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0063.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0064.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0065.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0066.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0067.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0068.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0069.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0070.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0071.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0072.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0073.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0074.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0075.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0076.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0077.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0078.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0079.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0080.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0081.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0082.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0083.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0084.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0085.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0086.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0087.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0088.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0089.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0090.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0091.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0092.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0093.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0094.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390028/0095.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390050/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390053/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390071/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390188/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390205/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390216/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390216/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390317/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390340/0021.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390354/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390354/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390489/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390489/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390621/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390621/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390689/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390705/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390705/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390706/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390711/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390711/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390711/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390757/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390777/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390781/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390781/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390787/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390826/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390845/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390845/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390845/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390845/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/390893/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391027/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391027/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391027/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391027/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0007.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0008.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0009.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0010.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0011.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0012.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0013.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0014.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0015.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0016.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0017.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0018.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0019.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0020.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0021.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0022.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0023.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0024.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0025.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0026.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0027.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0030.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0031.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0032.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0033.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0035.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0036.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0037.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0037a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0039.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0041.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0042.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0043.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0044.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0045.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0045a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0046.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0047.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0048.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0049.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0050.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0051.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0052.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0053.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0054.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0056.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0057.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0058.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0059.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0060.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0061.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0062.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0063.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0064.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0065.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0066.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0067.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0068.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0069.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0070.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0071.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0072.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0073.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0074.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0075.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0077.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0078.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0079.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0080.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0081.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0082.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0083.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0084.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0085.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0086.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0087.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0088.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0089.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0090.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0091.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0093.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0095.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0096.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0097.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0098.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0099.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0101.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0102.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0103.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0104.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0105.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0106.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0107.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0109.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0110.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0111.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0113.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0116.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0117.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0118.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0119.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0121.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0295.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0296.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0297.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0298.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0299.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0300.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0301.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0302.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0303.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0304.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0305.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0306.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0307.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0308.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0309.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0310.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0311.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0312.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0312a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0313.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0313a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0314.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0314a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0315.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0316.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0317.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0318.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0319.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0320.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0320a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0321.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0321a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0322.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0322a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0323.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0324.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0325.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0326.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0327.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0328.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0329.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0329a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0330.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0331.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0332.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0333.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0334.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0335.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0336.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0337.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0337a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0337b.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0338.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0339.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0339a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0341.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0342.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0343.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0344.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0345.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0346.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0347.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0347a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0348.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0349.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0350.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0351.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0352.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0353.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0354.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0355.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0356.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0357.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0358.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0358a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0359.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0360.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0360a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0361.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0362.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0363.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0364.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0365.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0366.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0366a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0367.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0368.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0369.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0370.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0371.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0372.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0373.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0373a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0374.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0375.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0375a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0376.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0377.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0377a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0377b.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0378.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0378a.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0379.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0380.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0381.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0382.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0383.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0384.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0385.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0386.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391098/0387.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391152/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391152/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0006.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0007.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0009.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0010.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0012.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0013.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0014.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0015.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0016.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391282/0017.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391314/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391314/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391352/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391352/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391352/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391352/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391353/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391353/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391364/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391364/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391373/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391373/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391392/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391392/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391393/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391393/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391394/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391465/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391465/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391506/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391506/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391507/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391507/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391510/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391510/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391510/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391510/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391525/0002.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391525/0003.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391525/0004.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/gsa/391525/0005.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/ub_basel/G_H_2301.xml',
	'/Users/gregor/Documents/Faustedition/data/db/xml/transcription/ub_leipzig/Sammlung_Hirzel/b497.xml'	
	]
	
convert()