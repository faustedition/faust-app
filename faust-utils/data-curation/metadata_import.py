#!/usr/bin/env python
# coding=UTF-8
#
# Import metadata from various sources
#

import codecs
import copy
import os
import os.path
import re
import sys

import lxml.etree

import faust

# Hierarchy mappings for WA references
wa_references = {
	('I 3', '213') : ("gedichte",),
	('I 3', '214') : ("gedichte",),
	('I 3', '215') : ("gedichte",),
	('I 3', '217') : ("gedichte",),
	('I 3', '219') : ("gedichte",),
	('I 3', '239', '2') : ("gedichte",),
	('I 3', '239', '3') : ("gedichte",),
	('I 3', '312', '1') : ("gedichte",),
	('I 3', '325', '3') : ("gedichte",),
	('I 3', '331', '2') : ("gedichte",),
	('I 4', '33') : ("gedichte",),
	('I 4', '107', '1') : ("gedichte",),
	('I 4', '282', '1') : ("gedichte",),
	('I 4', '282', '2') : ("gedichte",),
	('I 4', '283', '1') : ("gedichte",),
	('I 4', '283', '2') : ("gedichte",),
	('I 4', '294', '2') : ("gedichte",),
	('I 5.1', '50') : ("gedichte",),
	('I 5.1', '72') : ("gedichte",),
	('I 5.1', '189') : ("gedichte",),
	('I 5.2', '357', '2') : ("paralipomena",),
	('I 5.2', '358', '1') : ("paralipomena",),
	('I 5.2', '400', '2') : ("paralipomena",),
	('I 5.2', '420', '1') : ("paralipomena",),
	('I 6', '212') : ("west-oestlicher_divan", ),
	('I 6', '275') : ("west-oestlicher_divan", ),
	('I 14', '1') : ("faust", "1"),
	('I 14', '7') : ("faust", "0"),
	('I 14', '17') : ("faust", "0"),
	('I 14', '25') : ("faust", "1"),
	('I 14', '213') : ("faust", "1"),
	('I 14', '239') : ("faust", "1"),
	('I 14', '314') : ("faust", "1"),
	('I 15.1', '1') : ("faust", "2"),
	('I 15.1', '2') : ("faust", "2"),
	('I 15.1', '3') : ("faust", "2.1"),
	('I 15.1', '8') : ("faust", "2.1"),
	('I 15.1', '90') : ("faust", "2.2"),
	('I 15.1', '90-100') : ("faust", "2.2"),
	('I 15.1', '101') : ("faust", "2.2"),
	('I 15.1', '110') : ("faust", "2.2"),
	('I 15.1', '177') : ("faust", "2.3"),
	('I 15.1', '245') : ("faust", "2.4"),
	('I 15.1', '290') : ("faust", "2.5"),
	('I 15.1', '341') : ("faust", "2"),
	('I 15.1', '342') : ("faust", "2"),
	('I 15.1', '343') : ("faust", "2"),
	('I 15.1', '344') : ("faust", "2"),
	('I 15.1', '344-345') : ("faust", "2"),
	('I 41.2', '290') : ("kunst_und_alterthum", ),
	('I 42.2', '156', '5') : ("maximen_reflexionen", ),
	('I 42.2', '170', '5') : ("maximen_reflexionen", ),
	('I 42.2', '225', '3') : ("maximen_reflexionen", ),
	('I 42.2', '231', '3') : ("maximen_reflexionen", ),
	('I 42.2', '247', '2') : ("maximen_reflexionen", ),
	('I 42.2', '247', '3') : ("maximen_reflexionen", ),
	('II 11', '156', '5') : ("zur_naturwissenschaft", ),
	('LA I 11', '244') : ("faust", ),
	('LA I 11', '337') : ("faust", )
}

# GSA callnumber splitter
gsa_cn = lambda cn_str: tuple(re.split(r'[/,\*:]', cn_str))

def parse_allegro_file(allegro_file):
	"""Parses an Allegro-C file into a data structure"""
	records = list()
	for record_lines in codecs.open(allegro_file, encoding="utf-8").read().split("\n\n"):
		record = dict()
		last = None
		for field in record_lines.split("\n"):
			if field.startswith("#"):
				if len(field) > 4:
					last  = field[1:4].strip()
					record[last] = field[4:].strip()
				else:
					last = field[1:].strip()
					record[last] = ""
			elif last is not None:
				record[last] += field
			else:
				raise field
		for field in record.keys()[:]:
			if len(record[field]) == 0: del record[field]
		
		records.append(record)
	return records

# read GSA archive database
gsa_archive = list()
gsa_archive_db = lxml.etree.parse("metadata/gsa-archive-database.xml")
for item in gsa_archive_db.findall("ITEM"):
	record = dict()
	for field in item:
		record[field.tag.lower()] = field.text
	gsa_archive.append(record)

gsa_archive_index = dict()
for record in gsa_archive:
	cn = gsa_cn(record["bestandnr"] + "/" + record["signatur"])
	gsa_archive_index[cn] = record

print len(gsa_archive), "GSA archive database records read"

# read GSA Paralipomena mappings
paralip_mappings = parse_allegro_file("metadata/gsa-paralipomena-mapping.txt")

paralip_mappings_index = dict()
for m in paralip_mappings:
	callnumber = gsa_cn(m["08a"])
	if callnumber in paralip_mappings_index: 
		paralip_mappings_index[callnumber].append(m)
	else:
		paralip_mappings_index[callnumber] = [m]

print len(paralip_mappings), "GSA paralipomena mappings read"

# read GSA inventory database field descriptors
gsa_field_descrs = None
for record in parse_allegro_file("metadata/gsa-category-descriptions.txt"): gsa_field_descrs = record

# read GSA callnumber mappings
gsa_callnumber_mapping = dict()
gsa_callnumber = None
for l in open("metadata/gsa_callnumber_mapping.txt"):
	if gsa_callnumber is not None:
		old_callnumber = l.strip()
		gsa_callnumber_mapping[old_callnumber] = gsa_callnumber
		gsa_callnumber = None
	else:
		gsa_callnumber = l.strip()
		
print len(gsa_callnumber_mapping), "GSA callnumber mappings read"
	
def write_metadata_xml(metadata, root):
	fields = metadata.keys()
	fields.sort()
	for field in fields:
		field_xml = lxml.etree.SubElement(root, faust_ns + "field")

		key_xml = lxml.etree.SubElement(field_xml, faust_ns + "key")
		key_xml.set("n", field)
		key_xml.text = unicode(gsa_field_descrs[field])
		
		value_xml = lxml.etree.SubElement(field_xml, faust_ns + "value")
		value_xml.text = unicode(metadata[field])
	
# read GSA inventory database
gsa_inventory_db = parse_allegro_file("metadata/gsa-inventory-database.txt")
print len(gsa_inventory_db), "GSA inventory database records read"

# read ABR dissertation
abr_diss_xml = lxml.etree.parse("metadata/abr-dissertation-text.xml")
abr_diss_index = dict()
abr_diss_texts = abr_diss_xml.xpath("//text")
for t in abr_diss_texts:
	callnumber = re.sub(r'\s+', " ", t.find("paralipomenon").get("n")).strip()
	if callnumber.startswith("GSA"):
		callnumber = re.sub(r'\(WA[^\)]+\)', "", callnumber)
		callnumber = re.sub(r'GSA', "", callnumber)
		callnumber = re.sub(r'[\(\)]', "", callnumber)
		callnumber = re.sub(r'\s+', " ", callnumber).strip()
		callnumbers = callnumber.split("/")
		callnumbers = map(lambda x: x.strip(), callnumbers)
		callnumbers = map(lambda x: tuple(["25"] + re.split(r'[,\s]+', x)), callnumbers)
		for cn in callnumbers:
			if cn in abr_diss_index: 
				abr_diss_index[cn].append(t)
			else:
				abr_diss_index[cn] = [ t ]
print len(abr_diss_texts), "ABR paralipomena records read"

def cleaned_up_abr_commentary(src, dest):
	src = copy.deepcopy(src)
	for n_added in src.xpath(".//*[@type='hinzugefuegt']"): del n_added.attrib["type"]
	for p in src.xpath(".//p[normalize-space() = '']"): p.getparent().remove(p)
	lxml.etree.strip_tags(src, "hr", "br")
	dest.append(src)

# create metadata structure
faust_ns = faust.ns("f")
tei_ns = faust.ns("tei")
xml_ns = faust.ns("xml")

metadata_struct = dict()
documents_struct = dict()

gsa_documents = dict()
other_documents = dict()

for record in gsa_inventory_db:	
	cn = gsa_cn("08" in record and record["08"] or record["089"])
	gsa_ident = None
		
	document_xml = None
	imported_xml = None
	
	archive_record = None	
	if cn in gsa_archive_index: archive_record = gsa_archive_index[cn]
	if archive_record is None and cn[:-1] in gsa_archive_index: archive_record = gsa_archive_index[cn[:-1]]
	if archive_record is not None:
		if "ident" not in archive_record: raise Exception(repr(archive_record))
		gsa_ident = archive_record["ident"]
		if gsa_ident in gsa_documents:
			document_xml = gsa_documents[gsa_ident]
			imported_xml = document_xml.xpath("./f:metadataImport", namespaces=faust.namespaces)[0]
		else:
			document_xml = lxml.etree.Element(faust_ns + "materialUnit", nsmap=faust.namespaces)
			document_xml.set("type", "archival_unit")
			
			imported_xml = lxml.etree.SubElement(document_xml, faust_ns + "metadataImport")			
			archive_xml = lxml.etree.SubElement(imported_xml, faust_ns + "archiveDatabase")
			archive_xml.set(xml_ns + "space", "preserve")
			
			fields = archive_record.keys()
			fields.sort()
			for field in fields:
				field_xml = lxml.etree.SubElement(archive_xml, faust_ns + field)
				field_xml.text = unicode(archive_record[field])		
			
			# add ABR dissertation data
			abr_record = None
			if cn in abr_diss_index: abr_record = abr_diss_index[cn]
			if abr_record is None and cn[:-1] in abr_diss_index: abr_record = abr_diss_index[cn[:-1]]
			if abr_record is not None:
				abr_xml = lxml.etree.SubElement(imported_xml, faust_ns + "abrDissertation")
				for abr_paralip in abr_record:
					for metadata in abr_paralip.xpath("./metadaten"):
						abr_xml.append(copy.deepcopy(metadata))
					cleaned_up_abr_commentary(abr_paralip.xpath("./kommentar")[0], abr_xml)
			
			gsa_documents[gsa_ident] = document_xml
	else:
		continue

	# categorize record and add paralipomena records
	paralip_metadata_xml = list()	
	folder = None
	
	if "089" in record:
		folder = ("reproduktionen", )
	elif "11z" in record:
		folder = wa_references[tuple(record["11z"].split(","))]	
	elif "08" in record:
		cn = gsa_cn(record["08"])
		paralip_metadata = None
		if cn in paralip_mappings_index:
			folder = ("paralipomena", )
			paralip_metadata = paralip_mappings_index[cn]
		else:
			for p_cn in paralip_mappings_index:
				if p_cn[:len(cn)] == cn:
					folder = ("paralipomena", )
					paralip_metadata = paralip_mappings_index[p_cn]
		if paralip_metadata is not None:
			for paralip in paralip_metadata:
				paralip_metadata_xml.append(lxml.etree.Element(faust_ns + "paralipomenon"))
				write_metadata_xml(paralip, paralip_metadata_xml[-1])
			

	# add GSA inventory database records
	metadata_xml = lxml.etree.SubElement(imported_xml, faust_ns + "inventoryDatabase")
	write_metadata_xml(record, metadata_xml)
	for paralip_xml in paralip_metadata_xml: 
		metadata_xml.append(paralip_xml)
	
	# put record into category folder
	if gsa_ident is not None:
		if folder is None: folder = ("verschiedenes", )

		if gsa_ident in documents_struct:
			if folder not in documents_struct[gsa_ident]: documents_struct[gsa_ident].append(folder)
		else:
			documents_struct[gsa_ident] = [ folder ]
			
	#print lxml.etree.tostring(document_xml, pretty_print=True)
	
for gsa_ident in documents_struct:
	folders = documents_struct[gsa_ident]
	if len(folders) > 1 and ("verschiedenes", ) in folders:
		folders.remove(("verschiedenes", ))
	if len(folders) > 1 and ("paralipomena", ) in folders:	
		folders.remove(("paralipomena", ))
	if len(folders) > 1:
		folders.sort()
		folders.reverse()		
		for folder in folders:
			if "faust" == folder[0]:
				documents_struct[gsa_ident] = [ folder ]
				break
	if len(documents_struct[gsa_ident]) > 1:
		documents_struct[gsa_ident] = [ folders[-1] ]

for gsa_ident in gsa_documents:
	gathering_path = "/".join(("transcript", "gsa", gsa_ident))
	transcript_dir = faust.absolute_path(gathering_path)	
	document_xml = gsa_documents[gsa_ident]
	text = None
	pages = list()
	for f in os.listdir(transcript_dir):
		if not f.endswith(".xml"): continue
		f_ident = re.search(r'[0-9]+', f).group(0)
		if f_ident == gsa_ident: 
			text = f
			continue
		if int(f_ident) == 1: continue
		pages.append(f)
	if len(pages) > 0:
		document_xml.set(xml_ns + "base", "faust://xml/" + gathering_path + "/")		
		if text is not None:
			document_xml.set("transcript", text)
		
		last = None
		pages.sort()
		for p in pages:
			p_xml = lxml.etree.Element(faust_ns + "materialUnit")
			p_xml.set("type", "page")
			p_xml.set("transcript", p)
			if last is None:
				document_xml.insert(0, p_xml)
			else:
				last.addnext(p_xml)
			last = p_xml
	metadata_xml = lxml.etree.Element(faust_ns + "metadata")
	document_xml.insert(0, metadata_xml)
	
	lxml.etree.SubElement(metadata_xml, faust_ns + "archive").text = "gsa"
	
	callnumber = faust.xpath("//f:signatur/text()", document_xml)[0]
	if callnumber in gsa_callnumber_mapping:
		callnumber = gsa_callnumber_mapping[callnumber] + " (" + callnumber + ")" 
	lxml.etree.SubElement(metadata_xml, faust_ns + "callnumber").text = callnumber
			
	wa_id_matches = faust.xpath("//f:key[@n='25']/following::f:value", document_xml)
	if (len(wa_id_matches) > 0):
		wa_id = wa_id_matches[0].text
		if wa_id != "-" and wa_id != "oS":
			lxml.etree.SubElement(metadata_xml, faust_ns + "waId").text = wa_id
		
	xml_dir = faust.absolute_path("/".join(("document", ) + documents_struct[gsa_ident][0]))
	if not os.path.isdir(xml_dir): os.makedirs(xml_dir)
	document_xml.getroottree().write("/".join((xml_dir, "gsa_" + gsa_ident + ".xml")), encoding="UTF-8", pretty_print=True)