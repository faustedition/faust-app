#!/usr/bin/env python
# coding=UTF-8
#
# Convert old text-based encodings to document oriented ones
#

import copy
import os
import os.path
import re
import lxml.etree
import faust
import itertools
import query

def to_convert():
	text_content_xp = faust.xpath("normalize-space(//tei:text)")
	ge_document_content_xp = faust.xpath("normalize-space(//ge:document)")
	has_text_in = lambda xp, xml: (len(" ".join(xp(xml)).strip()) > 0)
	
	to_convert = list()
	for xml_file in faust.xml_files():
		path = faust.relative_path(xml_file).split("/")
		if path[0] != "transcript": continue	
		file_name = path[-1]
		if file_name[:-len(".xml")] == path[-2]: continue
		if int(re.search(r'[0-9]+', file_name).group(0)) == 1: continue
		if not faust.is_tei_document(xml_file):	continue
	
		xml = lxml.etree.parse(xml_file)
		if has_text_in(text_content_xp, xml) and not has_text_in(ge_document_content_xp, xml):
			to_convert.append(xml_file)
	return to_convert

def to_convert_unencoded():
	files = [faust.absolute_path(rel_file) for rel_file in static_to_convert()]
	return query.matches (files, "not(" + query.encoded_xp + ")")

def remove_keep_tail(element):
	''' Removes the element, retaining its tail. '''
	pre = element.getprevious()
	parent = element.getparent()
	if element.tail:
		if pre is not None:
			if pre.tail:
				pre.tail = pre.tail + element.tail
			else:
				pre.tail = element.tail
		else:
			if parent.text:
				parent.text = parent.text + element.tail
			else:
				parent.text = element.tail
	parent.remove(element)
	
def convert():
	tei_text_xp = faust.xpath("//tei:text")
	xml_id_cnt = 0
	
	for xml_file in [faust.absolute_path(rel) for rel in static_to_convert()]:
		print xml_file

		xml = lxml.etree.parse(xml_file)

		# ***** Documentary transcript *****
		
		# prepare <ge:document/> context
		root = lxml.etree.Element(faust.ns("ge") + "document", nsmap=faust.namespaces)
		root.set(faust.ns("xml") + "id", "converted")
		for child in xml.getroot():
			if child.tag not in (faust.ns("tei") + "teiHeader", faust.ns("tei") + "facsimile"):
				child.addprevious(root)
				break
		
		surface = lxml.etree.SubElement(root, faust.ns("tei") + "surface")
		for body in faust.xpath(".//tei:body", xml):
			body_zone = copy.deepcopy(body)
			body_zone.tag = faust.ns("tei") + "zone"
			surface.append(body_zone)

		root = surface

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
			att_vals = ["overwrite", "overwriting", "overwritiung"]
			def type_overwr_in_attributes(element): return element.get("type", "") in att_vals
			def rend_overwr_in_attributes(element): return element.get("rend", "") in att_vals
			children_with_type = filter(type_overwr_in_attributes, subst)
			children_with_rend = filter(rend_overwr_in_attributes, subst)
			# type attribute in substitution
			if type_overwr_in_attributes(subst):
				del subst.attrib["type"]
			# rend attribute in substitution
			elif rend_overwr_in_attributes(subst):
				del subst.attrib["rend"]
			# type attribute in a child (in add or del)
			elif children_with_type:
				for child in children_with_type:
					del child.attrib["type"]
			# rend attribute in a child
			elif children_with_rend:
				for child in children_with_rend:
					del child.attrib["rend"]
			else:
				continue
			subst.tag = faust.ns("f") + "overw"
			for del_xml in subst.findall(faust.ns("tei") + "del"):
				del_xml.tag = faust.ns("f") + "under"
			for add in subst.findall(faust.ns("tei") + "add"):
				add.tag = faust.ns("f") + "over"

		# <div type="cleared"/> becomes <ge:used spanTo="#...">
		for div in root.iter(faust.ns("tei") + "div"):
			if "type" in div.attrib:
				if div.attrib["type"] == "cleared":
					used = lxml.etree.Element(faust.ns("ge") + "used")
					div.addprevious(used)
					xml_id_cnt += 1
					anchor_id = str(xml_id_cnt)
					used.set("spanTo", "#" + anchor_id)
					for child in div.getchildren():
						div.remove(child)
						div.addprevious(child)
					div.tag = faust.ns("tei") + "anchor"
					div.set(faust.ns("xml") + "id", anchor_id)

		# throw away text structure tagging
		lxml.etree.strip_tags(root,\
			faust.ns("tei") + "div", faust.ns("tei") + "lg",\
			faust.ns("tei") + "sp", faust.ns("tei") + "subst",\
			faust.ns("tei") + "name", faust.ns("tei") + "addSpan")

		# remove Schroer numbers
		for l in root.iter(faust.ns("tei") + "l"): 
			if "n" in l.attrib: del l.attrib["n"]
		
		# create simple lines
		for line_element in ("speaker", "l", "p", "stage", "head", "ab"):
			line_elements = list(root.iter(faust.ns("tei") + line_element))
			for le in line_elements:
				if le.get("rend", "") in ["underline", "underlined", "centered unterline"]:
					hi = copy.deepcopy(le)
					hi.tag = faust.ns("tei") + "hi"
					le.clear()
					for attr in list(hi.attrib.keys()):
						if attr == "rend": continue
						le.set(attr, hi.get(attr))
						del hi.attrib[attr]
					le.append(hi)
				le.tag = faust.ns("ge") + "line"
		
		# turn deletions into <f:st/> by default
		for del_xml in root.iter(faust.ns("tei") + "del"):
			del_xml.tag = faust.ns("f") + "st"
			del_type = del_xml.get("rend", "")
			if del_type == "strikethrough" or del_type == "strikedthrough": 
				del del_xml.attrib["rend"]
			
		# rename tags for fixations
		for rewrite_tag in ("fix", "repetition"):
			for rewrite in root.iter(faust.ns("tei") + rewrite_tag):
				rewrite.tag = faust.ns("ge") + "rewrite"


		# rename semantic tags with @rend="underline"
		for sem_hi_tag in ("emph", "name"):
			for sem_hi in root.iter(faust.ns("tei") + sem_hi_tag):
				if sem_hi.get("rend", "") == "underline":
					sem_hi.tag = faust.ns("tei") + "hi"
		
		# convert umlaut corrections
		umlaut_mapping = { 
			u"ä":u"a", u"Ä":u"A", 
			u"ö":u"o", u"Ö":u"O", 
			u"ü":u"u", u"Ü":u"U" 
			}
		corr_or_reg = itertools.chain(root.iter(faust.ns("tei") + "corr"), root.iter(faust.ns("tei") + "reg"))
		for element in corr_or_reg:
			for umlaut in umlaut_mapping:
				if element.text == umlaut:
					element.text = umlaut_mapping[umlaut]
					element.tag = faust.ns("tei") + "orig"
					
		# join lines with @rend='inline'
		for inline_line in list(faust.xpath(".//ge:line[@rend='inline']", root)):
			prev_lines = faust.xpath("./preceding::ge:line", inline_line)
			if len(prev_lines) == 0: continue
			prev_line = prev_lines[-1]
			
			if inline_line.text is None: 
				inline_line.text = " "
			else:
				inline_line.text += " "				
			inline_line.getparent().remove(inline_line)
			prev_line.append(inline_line)
			lxml.etree.strip_tags(prev_line, faust.ns("ge") + "line")
			
		# convert inline <lb/> to <ge:line/>
		for lb in list(root.iter(faust.ns("tei") + "lb")):
			parent = lb.getparent()
			if parent.tag != (faust.ns("ge") + "line"): continue

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
		
		# put <note/> in zones		
		for note in list(root.iter(faust.ns("tei") + "note")):
			parent = surface
			if len(faust.xpath(".//ge:line", note)) == 0:
				parent = lxml.etree.SubElement(parent, faust.ns("tei") + "zone")
				note.tag = faust.ns("ge") + "line"
			else:
				note.tag = faust.ns("tei") + "zone"
			note.getparent().remove(note)			
			parent.append(note)
			if "place" in note.attrib: del note.attrib["place"]


		# u<ex>nd</ex> becomes "und"
		for ex in root.iter(faust.ns("tei") + "ex"):
			try: pre_sibling = ex.itersiblings(preceding=True).next()
			except StopIteration: continue
			if pre_sibling.text:
				if re.split("\s+", pre_sibling.text).pop() == "u":
					# only in two files, do nothing
					pass

		# <abbr>u</abbr> becomes "und"
		for abbr in root.iter(faust.ns("tei") + "abbr"):
			if abbr.text == "u":
				tail = abbr.tail
				if tail: abbr.tail = "und" + tail
				else: abbr.tail = "und"
				remove_keep_tail(abbr)
				#abbr.tag = None

		# </ex> outside of <abbr/> becomes <supplied/>
		for ex in root.iter(faust.ns("tei") + "ex"):
			pass
			if not list(ex.iterancestors(faust.ns("tei") + "abbr")):
				ex.tag = faust.ns("tei") + "supplied"

		# <delSpan/> becomes <f:st/>
		for delSpan in root.iter(faust.ns("tei") + "delSpan"):
			delSpan.tag = faust.ns("f") + "st"

		

		# detach marginal elements
		for margin in list(faust.xpath(".//*[@place]", root)):
			place = margin.get("place")
			if place not in ("margin",\
			 	"top", "top-left", "topleft", "top-right", "topright",\
				"bottom", "bottom-left", "bottomleft", "bottom-right", "bottomright"):
				continue

			del margin.attrib["place"]
			parent = margin.getparent()
			
			margin_zone = lxml.etree.Element(faust.ns("tei") + "zone")
			if place.startswith("top"):
				surface.insert(0, margin_zone)
			else:
				surface.append(margin_zone)
				
			margin_parent = margin_zone
			if margin.tag != faust.ns("ge") + "line":
				margin_parent = lxml.etree.SubElement(margin_parent, faust.ns("ge") + "line")
				
			for ancestor in margin.iterancestors(faust.ns("ge") + "line"):
				line_id = ancestor.get(faust.ns("xml") + "id", None)
				if line_id is None:
					xml_id_cnt += 1
					line_id = "line_" + str(xml_id_cnt)
					ancestor.set(faust.ns("xml") + "id", line_id)
				margin_zone.set(faust.ns("f") + "top", "#" + line_id)
				break
			
			parent.remove(margin)
			margin_parent.append(margin)
			
		# detach interlinear additions
		for inter_add in list(faust.xpath(".//tei:add[@place='above' or @place='below']", root)):
			line = None
			for ancestor in inter_add.iterancestors(faust.ns("ge") + "line"):
				line = ancestor
				break
			if line is None: raise Exception(lxml.etree.tostring(inter_add))
			
			adjunct_line = None
			if inter_add.get("place") == "above":
				adjunct_line = line.getprevious()
			else:
				adjunct_line = line.getnext()
			if (adjunct_line is None) or (adjunct_line.tag != (faust.ns("ge") + "line")) or\
				(adjunct_line.get("type", "") != "inter"):
				adjunct_line = lxml.etree.Element(faust.ns("ge") + "line")
				adjunct_line.set("type", "inter")
				if inter_add.get("place") == "above":
					line.addprevious(adjunct_line)
				else:
					line.addnext(adjunct_line)
			
			xml_id_cnt += 1
			anchor_id = "anchor_" + str(xml_id_cnt)
			
			ins_mark = lxml.etree.SubElement(adjunct_line, faust.ns("f") + "ins")
			ins_mark.set(faust.ns("f") + "at", "#" + anchor_id)
			
			ins_mark.tail = inter_add.text
			inter_add.text = None
			inter_add.tag = faust.ns("tei") + "anchor"
			inter_add.set(faust.ns("xml") + "id", anchor_id)
			for child in inter_add.getchildren():
				inter_add.remove(child)
				adjunct_line.append(child)
			del inter_add.attrib["place"]
			
		# remove remaining <add/> elements
		lxml.etree.strip_tags(root, faust.ns("tei") + "add")
		
		# remove <lb/>s, which are located in zones after conversion
		for lb in list(root.iter(faust.ns("tei") + "lb")):
			parent = lb.getparent()
			if parent.tag == (faust.ns("tei") + "zone"):
				parent.remove(lb)

			
		

		# convert some attribute values
		for typed in faust.xpath(".//*[@type='foliation']" , root):
			typed.set("type", "folioNum")
		for typed in faust.xpath(".//*[@type='sigel']" , root):
			typed.set("type", "sig")
		for typed in faust.xpath(".//*[@type='sigil']" , root):
			typed.set("type", "sig")

		# "#_bl", "#_t" u.ä. → "#sc_bl" etc.
		for any_elem in root.iter(tag=lxml.etree.Element):
			for attrib in any_elem.attrib:
				val = any_elem.get(attrib)
				if val.startswith("#_"):
					new_val ="#sc" + val[1:]
					any_elem.set(attrib, new_val)
				
		#remove type attributes for certain values
		for typed in faust.xpath(".//*[@type='instant' or @type='inst' or @type='instantrevision'\
		or @type='late' or @type='soon']" , root):
			del typed.attrib["type"]

		
		# ***** Textual transcript *****
		
		for text in faust.xpath(".//tei:text", xml):

			# remove hand attribute
			for hand_attributed in faust.xpath(".//*[@hand]", text):
				del hand_attributed.attrib["hand"]
			#remove handShifts
			lxml.etree.strip_tags(text, faust.ns("tei") + "handShift")

			# convert umlaut corrections
			corr_or_reg = itertools.chain(text.iter(faust.ns("tei") + "corr"), root.iter(faust.ns("tei") + "reg"))
			for element in corr_or_reg:
				for umlaut in umlaut_mapping:
					if element.text == umlaut:
						element.text = umlaut_mapping[umlaut]
						element.tag = faust.ns("tei") + "orig"

		# write the converted file
		path = ("conversion_test/" + faust.relative_path(xml_file)).split("/")
		path[-1] = "conv_" + path[-1]
		dir_path = "/".join(path[:-1])
		if not os.path.isdir(dir_path): os.makedirs(dir_path)

		xml.write("/".join(path), encoding="UTF-8")

def queries():

	''' Some queries of the current status '''
	
	print "to convert: ", len(to_convert_unencoded())
	print "of: ", len(static_to_convert())

	## print "***** values for rend attribute *****"
	## values = query.unique_values(to_convert_unencoded(), "//@rend")
	## for value in sorted(values): print value

	## print "***** values for type attribute *****"
	## type_values = query.unique_values(to_convert_unencoded(), "//@type")
	## for value in sorted(type_values): print value

	## print "***** values for hand attribute *****"
	## values = query.unique_values(to_convert_unencoded(), "//@hand")
	## for value in sorted(values): print value

	## print "***** values for new  attribute *****"
	## values = query.unique_values(to_convert_unencoded(), "//@new")
	## for value in sorted(values): print value

	## print '************* end queries *****************'

def static_to_convert():
	return [
	'transcript/gsa/389872/0002.xml',
	'transcript/gsa/389872/0003.xml',
	'transcript/gsa/389872/0004.xml',
	'transcript/gsa/389872/0005.xml',
	'transcript/gsa/389877/0002.xml',
	'transcript/gsa/389877/0003.xml',
	'transcript/gsa/389890/0002.xml',
	'transcript/gsa/389890/0003.xml',
	'transcript/gsa/389890/0004.xml',
	'transcript/gsa/389892/0002.xml',
	'transcript/gsa/389892/0003.xml',
	'transcript/gsa/389992/0008.xml',
	'transcript/gsa/390028/0002.xml',
	'transcript/gsa/390028/0003.xml',
	'transcript/gsa/390028/0004.xml',
	'transcript/gsa/390028/0005.xml',
	'transcript/gsa/390028/0006.xml',
	'transcript/gsa/390028/0007.xml',
	'transcript/gsa/390028/0008.xml',
	'transcript/gsa/390028/0009.xml',
	'transcript/gsa/390028/0010.xml',
	'transcript/gsa/390028/0011.xml',
	'transcript/gsa/390028/0012.xml',
	'transcript/gsa/390028/0013.xml',
	'transcript/gsa/390028/0014.xml',
	'transcript/gsa/390028/0015.xml',
	'transcript/gsa/390028/0016.xml',
	'transcript/gsa/390028/0017.xml',
	'transcript/gsa/390028/0018.xml',
	'transcript/gsa/390028/0019.xml',
	'transcript/gsa/390028/0020.xml',
	'transcript/gsa/390028/0021.xml',
	'transcript/gsa/390028/0022.xml',
	'transcript/gsa/390028/0023.xml',
	'transcript/gsa/390028/0024.xml',
	'transcript/gsa/390028/0025.xml',
	'transcript/gsa/390028/0026.xml',
	'transcript/gsa/390028/0027.xml',
	'transcript/gsa/390028/0028.xml',
	'transcript/gsa/390028/0029.xml',
	'transcript/gsa/390028/0030.xml',
	'transcript/gsa/390028/0031.xml',
	'transcript/gsa/390028/0032.xml',
	'transcript/gsa/390028/0033.xml',
	'transcript/gsa/390028/0034.xml',
	'transcript/gsa/390028/0035.xml',
	'transcript/gsa/390028/0036.xml',
	'transcript/gsa/390028/0037.xml',
	'transcript/gsa/390028/0038.xml',
	'transcript/gsa/390028/0039.xml',
	'transcript/gsa/390028/0040.xml',
	'transcript/gsa/390028/0041.xml',
	'transcript/gsa/390028/0042.xml',
	'transcript/gsa/390028/0043.xml',
	'transcript/gsa/390028/0044.xml',
	'transcript/gsa/390028/0045.xml',
	'transcript/gsa/390028/0046.xml',
	'transcript/gsa/390028/0047.xml',
	'transcript/gsa/390028/0048.xml',
	'transcript/gsa/390028/0049.xml',
	'transcript/gsa/390028/0050.xml',
	'transcript/gsa/390028/0051.xml',
	'transcript/gsa/390028/0052.xml',
	'transcript/gsa/390028/0053.xml',
	'transcript/gsa/390028/0054.xml',
	'transcript/gsa/390028/0055.xml',
	'transcript/gsa/390028/0056.xml',
	'transcript/gsa/390028/0057.xml',
	'transcript/gsa/390028/0058.xml',
	'transcript/gsa/390028/0059.xml',
	'transcript/gsa/390028/0060.xml',
	'transcript/gsa/390028/0061.xml',
	'transcript/gsa/390028/0062.xml',
	'transcript/gsa/390028/0063.xml',
	'transcript/gsa/390028/0064.xml',
	'transcript/gsa/390028/0065.xml',
	'transcript/gsa/390028/0066.xml',
	'transcript/gsa/390028/0067.xml',
	'transcript/gsa/390028/0068.xml',
	'transcript/gsa/390028/0069.xml',
	'transcript/gsa/390028/0070.xml',
	'transcript/gsa/390028/0071.xml',
	'transcript/gsa/390028/0072.xml',
	'transcript/gsa/390028/0073.xml',
	'transcript/gsa/390028/0074.xml',
	'transcript/gsa/390028/0075.xml',
	'transcript/gsa/390028/0076.xml',
	'transcript/gsa/390028/0077.xml',
	'transcript/gsa/390028/0078.xml',
	'transcript/gsa/390028/0079.xml',
	'transcript/gsa/390028/0080.xml',
	'transcript/gsa/390028/0081.xml',
	'transcript/gsa/390028/0082.xml',
	'transcript/gsa/390028/0083.xml',
	'transcript/gsa/390028/0084.xml',
	'transcript/gsa/390028/0085.xml',
	'transcript/gsa/390028/0086.xml',
	'transcript/gsa/390028/0087.xml',
	'transcript/gsa/390028/0088.xml',
	'transcript/gsa/390028/0089.xml',
	'transcript/gsa/390028/0090.xml',
	'transcript/gsa/390028/0091.xml',
	'transcript/gsa/390028/0092.xml',
	'transcript/gsa/390028/0093.xml',
	'transcript/gsa/390028/0094.xml',
	'transcript/gsa/390028/0095.xml',
	'transcript/gsa/390050/0002.xml',
	'transcript/gsa/390053/0002.xml',
	'transcript/gsa/390071/0002.xml',
	'transcript/gsa/390188/0002.xml',
	'transcript/gsa/390205/0002.xml',
	'transcript/gsa/390216/0002.xml',
	'transcript/gsa/390216/0003.xml',
	'transcript/gsa/390317/0002.xml',
	'transcript/gsa/390340/0021.xml',
	'transcript/gsa/390354/0002.xml',
	'transcript/gsa/390354/0003.xml',
	'transcript/gsa/390489/0002.xml',
	'transcript/gsa/390489/0003.xml',
	'transcript/gsa/390621/0002.xml',
	'transcript/gsa/390621/0003.xml',
	'transcript/gsa/390689/0002.xml',
	'transcript/gsa/390705/0002.xml',
	'transcript/gsa/390705/0003.xml',
	'transcript/gsa/390706/0003.xml',
	'transcript/gsa/390711/0002.xml',
	'transcript/gsa/390711/0003.xml',
	'transcript/gsa/390711/0004.xml',
	'transcript/gsa/390757/0002.xml',
	'transcript/gsa/390777/0002.xml',
	'transcript/gsa/390781/0002.xml',
	'transcript/gsa/390781/0003.xml',
	'transcript/gsa/390787/0002.xml',
	'transcript/gsa/390826/0002.xml',
	'transcript/gsa/390845/0002.xml',
	'transcript/gsa/390845/0003.xml',
	'transcript/gsa/390845/0004.xml',
	'transcript/gsa/390845/0005.xml',
	'transcript/gsa/390893/0002.xml',
	'transcript/gsa/391027/0002.xml',
	'transcript/gsa/391027/0003.xml',
	'transcript/gsa/391027/0004.xml',
	'transcript/gsa/391027/0005.xml',
	'transcript/gsa/391098/0007.xml',
	'transcript/gsa/391098/0008.xml',
	'transcript/gsa/391098/0009.xml',
	'transcript/gsa/391098/0010.xml',
	'transcript/gsa/391098/0011.xml',
	'transcript/gsa/391098/0012.xml',
	'transcript/gsa/391098/0013.xml',
	'transcript/gsa/391098/0014.xml',
	'transcript/gsa/391098/0015.xml',
	'transcript/gsa/391098/0016.xml',
	'transcript/gsa/391098/0017.xml',
	'transcript/gsa/391098/0018.xml',
	'transcript/gsa/391098/0019.xml',
	'transcript/gsa/391098/0020.xml',
	'transcript/gsa/391098/0021.xml',
	'transcript/gsa/391098/0022.xml',
	'transcript/gsa/391098/0023.xml',
	'transcript/gsa/391098/0024.xml',
	'transcript/gsa/391098/0025.xml',
	'transcript/gsa/391098/0026.xml',
	'transcript/gsa/391098/0027.xml',
	'transcript/gsa/391098/0030.xml',
	'transcript/gsa/391098/0031.xml',
	'transcript/gsa/391098/0032.xml',
	'transcript/gsa/391098/0033.xml',
	'transcript/gsa/391098/0035.xml',
	'transcript/gsa/391098/0036.xml',
	'transcript/gsa/391098/0037.xml',
	'transcript/gsa/391098/0037a.xml',
	'transcript/gsa/391098/0039.xml',
	'transcript/gsa/391098/0041.xml',
	'transcript/gsa/391098/0042.xml',
	'transcript/gsa/391098/0043.xml',
	'transcript/gsa/391098/0044.xml',
	'transcript/gsa/391098/0045.xml',
	'transcript/gsa/391098/0045a.xml',
	'transcript/gsa/391098/0046.xml',
	'transcript/gsa/391098/0047.xml',
	'transcript/gsa/391098/0048.xml',
	'transcript/gsa/391098/0049.xml',
	'transcript/gsa/391098/0050.xml',
	'transcript/gsa/391098/0051.xml',
	'transcript/gsa/391098/0052.xml',
	'transcript/gsa/391098/0053.xml',
	'transcript/gsa/391098/0054.xml',
	'transcript/gsa/391098/0056.xml',
	'transcript/gsa/391098/0057.xml',
	'transcript/gsa/391098/0058.xml',
	'transcript/gsa/391098/0059.xml',
	'transcript/gsa/391098/0060.xml',
	'transcript/gsa/391098/0061.xml',
	'transcript/gsa/391098/0062.xml',
	'transcript/gsa/391098/0063.xml',
	'transcript/gsa/391098/0064.xml',
	'transcript/gsa/391098/0065.xml',
	'transcript/gsa/391098/0066.xml',
	'transcript/gsa/391098/0067.xml',
	'transcript/gsa/391098/0068.xml',
	'transcript/gsa/391098/0069.xml',
	'transcript/gsa/391098/0070.xml',
	'transcript/gsa/391098/0071.xml',
	'transcript/gsa/391098/0072.xml',
	'transcript/gsa/391098/0073.xml',
	'transcript/gsa/391098/0074.xml',
	'transcript/gsa/391098/0075.xml',
	'transcript/gsa/391098/0077.xml',
	'transcript/gsa/391098/0078.xml',
	'transcript/gsa/391098/0079.xml',
	'transcript/gsa/391098/0080.xml',
	'transcript/gsa/391098/0081.xml',
	'transcript/gsa/391098/0082.xml',
	'transcript/gsa/391098/0083.xml',
	'transcript/gsa/391098/0084.xml',
	'transcript/gsa/391098/0085.xml',
	'transcript/gsa/391098/0086.xml',
	'transcript/gsa/391098/0087.xml',
	'transcript/gsa/391098/0088.xml',
	'transcript/gsa/391098/0089.xml',
	'transcript/gsa/391098/0090.xml',
	'transcript/gsa/391098/0091.xml',
	'transcript/gsa/391098/0093.xml',
	'transcript/gsa/391098/0095.xml',
	'transcript/gsa/391098/0096.xml',
	'transcript/gsa/391098/0097.xml',
	'transcript/gsa/391098/0098.xml',
	'transcript/gsa/391098/0099.xml',
	'transcript/gsa/391098/0101.xml',
	'transcript/gsa/391098/0102.xml',
	'transcript/gsa/391098/0103.xml',
	'transcript/gsa/391098/0104.xml',
	'transcript/gsa/391098/0105.xml',
	'transcript/gsa/391098/0106.xml',
	'transcript/gsa/391098/0107.xml',
	'transcript/gsa/391098/0109.xml',
	'transcript/gsa/391098/0110.xml',
	'transcript/gsa/391098/0111.xml',
	'transcript/gsa/391098/0113.xml',
	'transcript/gsa/391098/0116.xml',
	'transcript/gsa/391098/0117.xml',
	'transcript/gsa/391098/0118.xml',
	'transcript/gsa/391098/0119.xml',
	'transcript/gsa/391098/0121.xml',
	'transcript/gsa/391098/0295.xml',
	'transcript/gsa/391098/0296.xml',
	'transcript/gsa/391098/0297.xml',
	'transcript/gsa/391098/0298.xml',
	'transcript/gsa/391098/0299.xml',
	'transcript/gsa/391098/0300.xml',
	'transcript/gsa/391098/0301.xml',
	'transcript/gsa/391098/0302.xml',
	'transcript/gsa/391098/0303.xml',
	'transcript/gsa/391098/0304.xml',
	'transcript/gsa/391098/0305.xml',
	'transcript/gsa/391098/0306.xml',
	'transcript/gsa/391098/0307.xml',
	'transcript/gsa/391098/0308.xml',
	'transcript/gsa/391098/0309.xml',
	'transcript/gsa/391098/0310.xml',
	'transcript/gsa/391098/0311.xml',
	'transcript/gsa/391098/0312.xml',
	'transcript/gsa/391098/0312a.xml',
	'transcript/gsa/391098/0313.xml',
	'transcript/gsa/391098/0313a.xml',
	'transcript/gsa/391098/0314.xml',
	'transcript/gsa/391098/0314a.xml',
	'transcript/gsa/391098/0315.xml',
	'transcript/gsa/391098/0316.xml',
	'transcript/gsa/391098/0317.xml',
	'transcript/gsa/391098/0318.xml',
	'transcript/gsa/391098/0319.xml',
	'transcript/gsa/391098/0320.xml',
	'transcript/gsa/391098/0320a.xml',
	'transcript/gsa/391098/0321.xml',
	'transcript/gsa/391098/0321a.xml',
	'transcript/gsa/391098/0322.xml',
	'transcript/gsa/391098/0322a.xml',
	'transcript/gsa/391098/0323.xml',
	'transcript/gsa/391098/0324.xml',
	'transcript/gsa/391098/0325.xml',
	'transcript/gsa/391098/0326.xml',
	'transcript/gsa/391098/0327.xml',
	'transcript/gsa/391098/0328.xml',
	'transcript/gsa/391098/0329.xml',
	'transcript/gsa/391098/0329a.xml',
	'transcript/gsa/391098/0330.xml',
	'transcript/gsa/391098/0331.xml',
	'transcript/gsa/391098/0332.xml',
	'transcript/gsa/391098/0333.xml',
	'transcript/gsa/391098/0334.xml',
	'transcript/gsa/391098/0335.xml',
	'transcript/gsa/391098/0336.xml',
	'transcript/gsa/391098/0337.xml',
	'transcript/gsa/391098/0337a.xml',
	'transcript/gsa/391098/0337b.xml',
	'transcript/gsa/391098/0338.xml',
	'transcript/gsa/391098/0339.xml',
	'transcript/gsa/391098/0339a.xml',
	'transcript/gsa/391098/0341.xml',
	'transcript/gsa/391098/0342.xml',
	'transcript/gsa/391098/0343.xml',
	'transcript/gsa/391098/0344.xml',
	'transcript/gsa/391098/0345.xml',
	'transcript/gsa/391098/0346.xml',
	'transcript/gsa/391098/0347.xml',
	'transcript/gsa/391098/0347a.xml',
	'transcript/gsa/391098/0348.xml',
	'transcript/gsa/391098/0349.xml',
	'transcript/gsa/391098/0350.xml',
	'transcript/gsa/391098/0351.xml',
	'transcript/gsa/391098/0352.xml',
	'transcript/gsa/391098/0353.xml',
	'transcript/gsa/391098/0354.xml',
	'transcript/gsa/391098/0355.xml',
	'transcript/gsa/391098/0356.xml',
	'transcript/gsa/391098/0357.xml',
	'transcript/gsa/391098/0358.xml',
	'transcript/gsa/391098/0358a.xml',
	'transcript/gsa/391098/0359.xml',
	'transcript/gsa/391098/0360.xml',
	'transcript/gsa/391098/0360a.xml',
	'transcript/gsa/391098/0361.xml',
	'transcript/gsa/391098/0362.xml',
	'transcript/gsa/391098/0363.xml',
	'transcript/gsa/391098/0364.xml',
	'transcript/gsa/391098/0365.xml',
	'transcript/gsa/391098/0366.xml',
	'transcript/gsa/391098/0366a.xml',
	'transcript/gsa/391098/0367.xml',
	'transcript/gsa/391098/0368.xml',
	'transcript/gsa/391098/0369.xml',
	'transcript/gsa/391098/0370.xml',
	'transcript/gsa/391098/0371.xml',
	'transcript/gsa/391098/0372.xml',
	'transcript/gsa/391098/0373.xml',
	'transcript/gsa/391098/0373a.xml',
	'transcript/gsa/391098/0374.xml',
	'transcript/gsa/391098/0375.xml',
	'transcript/gsa/391098/0375a.xml',
	'transcript/gsa/391098/0376.xml',
	'transcript/gsa/391098/0377.xml',
	'transcript/gsa/391098/0377a.xml',
	'transcript/gsa/391098/0377b.xml',
	'transcript/gsa/391098/0378.xml',
	'transcript/gsa/391098/0378a.xml',
	'transcript/gsa/391098/0379.xml',
	'transcript/gsa/391098/0380.xml',
	'transcript/gsa/391098/0381.xml',
	'transcript/gsa/391098/0382.xml',
	'transcript/gsa/391098/0383.xml',
	'transcript/gsa/391098/0384.xml',
	'transcript/gsa/391098/0385.xml',
	'transcript/gsa/391098/0386.xml',
	'transcript/gsa/391098/0387.xml',
	'transcript/gsa/391152/0002.xml',
	'transcript/gsa/391152/0003.xml',
	'transcript/gsa/391282/0002.xml',
	'transcript/gsa/391282/0003.xml',
	'transcript/gsa/391282/0005.xml',
	'transcript/gsa/391282/0006.xml',
	'transcript/gsa/391282/0007.xml',
	'transcript/gsa/391282/0009.xml',
	'transcript/gsa/391282/0010.xml',
	'transcript/gsa/391282/0012.xml',
	'transcript/gsa/391282/0013.xml',
	'transcript/gsa/391282/0014.xml',
	'transcript/gsa/391282/0015.xml',
	'transcript/gsa/391282/0016.xml',
	'transcript/gsa/391282/0017.xml',
	'transcript/gsa/391314/0002.xml',
	'transcript/gsa/391314/0003.xml',
	'transcript/gsa/391352/0002.xml',
	'transcript/gsa/391352/0003.xml',
	'transcript/gsa/391352/0004.xml',
	'transcript/gsa/391352/0005.xml',
	'transcript/gsa/391353/0002.xml',
	'transcript/gsa/391353/0003.xml',
	'transcript/gsa/391364/0002.xml',
	'transcript/gsa/391364/0003.xml',
	'transcript/gsa/391373/0002.xml',
	'transcript/gsa/391373/0003.xml',
	'transcript/gsa/391392/0002.xml',
	'transcript/gsa/391392/0003.xml',
	'transcript/gsa/391393/0002.xml',
	'transcript/gsa/391393/0003.xml',
	'transcript/gsa/391394/0003.xml',
	'transcript/gsa/391465/0002.xml',
	'transcript/gsa/391465/0003.xml',
	'transcript/gsa/391506/0002.xml',
	'transcript/gsa/391506/0003.xml',
	'transcript/gsa/391507/0002.xml',
	'transcript/gsa/391507/0003.xml',
	'transcript/gsa/391510/0002.xml',
	'transcript/gsa/391510/0003.xml',
	'transcript/gsa/391510/0004.xml',
	'transcript/gsa/391510/0005.xml',
	'transcript/gsa/391525/0002.xml',
	'transcript/gsa/391525/0003.xml',
	'transcript/gsa/391525/0004.xml',
	'transcript/gsa/391525/0005.xml',
	'transcript/ub_basel/G_H_2301.xml',
	'transcript/ub_leipzig/Sammlung_Hirzel/b497.xml'	
	]
	
if __name__ == "__main__":
	queries()
	convert()
