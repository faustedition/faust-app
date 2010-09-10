#!/usr/bin/env python
# coding=UTF-8
#
# Import metadata from various sources
#

import re
import lxml.etree

import faust

# Hierarchy mappings for WA references
wa_references = {
	('I 3', '213') : ("Gedichte",),
	('I 3', '214') : ("Gedichte",),
	('I 3', '215') : ("Gedichte",),
	('I 3', '217') : ("Gedichte",),
	('I 3', '219') : ("Gedichte",),
	('I 3', '239', '2') : ("Gedichte",),
	('I 3', '239', '3') : ("Gedichte",),
	('I 3', '312', '1') : ("Gedichte",),
	('I 3', '325', '3') : ("Gedichte",),
	('I 3', '331', '2') : ("Gedichte",),
	('I 4', '33') : ("Gedichte",),
	('I 4', '107', '1') : ("Gedichte",),
	('I 4', '282', '1') : ("Gedichte",),
	('I 4', '282', '2') : ("Gedichte",),
	('I 4', '283', '1') : ("Gedichte",),
	('I 4', '283', '2') : ("Gedichte",),
	('I 4', '294', '2') : ("Gedichte",),
	('I 5.1', '50') : ("Gedichte",),
	('I 5.1', '72') : ("Gedichte",),
	('I 5.1', '189') : ("Gedichte",),
	('I 5.2', '357', '2') : ("Paralipomena", "WA"),
	('I 5.2', '358', '1') : ("Paralipomena", "WA"),
	('I 5.2', '400', '2') : ("Paralipomena", "WA"),
	('I 5.2', '420', '1') : ("Paralipomena", "WA"),
	('I 6', '212') : ("West-oestlicher_Divan", ),
	('I 6', '275') : ("West-oestlicher_Divan", ),
	('I 14', '1') : ("Faust", "1"),
	('I 14', '7') : ("Faust", "0"),
	('I 14', '17') : ("Faust", "0"),
	('I 14', '25') : ("Faust", "1"),
	('I 14', '213') : ("Faust", "1"),
	('I 14', '239') : ("Faust", "1"),
	('I 14', '314') : ("Faust", "1"),
	('I 15.1', '1') : ("Faust", "2"),
	('I 15.1', '2') : ("Faust", "2"),
	('I 15.1', '3') : ("Faust", "2.1"),
	('I 15.1', '8') : ("Faust", "2.1"),
	('I 15.1', '90') : ("Faust", "2.2"),
	('I 15.1', '90-100') : ("Faust", "2.2"),
	('I 15.1', '101') : ("Faust", "2.2"),
	('I 15.1', '110') : ("Faust", "2.2"),
	('I 15.1', '177') : ("Faust", "2.3"),
	('I 15.1', '245') : ("Faust", "2.4"),
	('I 15.1', '290') : ("Faust", "2.5"),
	('I 15.1', '341') : ("Faust", "2"),
	('I 15.1', '342') : ("Faust", "2"),
	('I 15.1', '343') : ("Faust", "2"),
	('I 15.1', '344') : ("Faust", "2"),
	('I 15.1', '344-345') : ("Faust", "2"),
	('I 41.2', '290') : ("Kunst_und_Alterthum", ),
	('I 42.2', '156', '5') : ("Maximen_Reflexionen", ),
	('I 42.2', '170', '5') : ("Maximen_Reflexionen", ),
	('I 42.2', '225', '3') : ("Maximen_Reflexionen", ),
	('I 42.2', '231', '3') : ("Maximen_Reflexionen", ),
	('I 42.2', '247', '2') : ("Maximen_Reflexionen", ),
	('I 42.2', '247', '3') : ("Maximen_Reflexionen", ),
	('II 11', '156', '5') : ("Zur_Naturwissenschaft", ),
	('LA I 11', '244') : ("Faust", ),
	('LA I 11', '337') : ("Faust", )
}

# GSA callnumber splitter
gsa_cn = lambda cn_str: tuple(re.split(r'[/,\*:]', cn_str))

def parse_allegro_file(allegro_file):
	"""Parses an Allegro-C file into a data structure"""
	records = list()
	for record_lines in allegro_file.read().split("\n\n"):
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
paralip_mappings = parse_allegro_file(file("metadata/gsa-paralipomena-mapping.txt"))

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
for record in parse_allegro_file(file("metadata/gsa-category-descriptions.txt")): gsa_field_descrs = record

# read GSA inventory database
gsa_inventory_db = parse_allegro_file(file("metadata/gsa-inventory-database.txt"))
print len(gsa_inventory_db), "GSA inventory database records read"

# read ABR dissertation
abr_diss_xml = lxml.etree.parse("metadata/abr-dissertation-text.xml")
ab_diss_index = dict()
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
			if cn in ab_diss_index: 
				ab_diss_index[cn].append(t)
			else:
				ab_diss_index[cn] = [ t ]
print len(abr_diss_texts), "ABR paralipomena records read"

# create metadata structure
metadata_struct = dict()
for record in gsa_inventory_db:
	if "xx0" not in record: raise repr(record)
	
	cn = gsa_cn("08" in record and record["08"] or record["089"])
	
	archive_record = None
	if cn in gsa_archive_index: archive_record = gsa_archive_index[cn]
	if archive_record is None and cn[:-1] in gsa_archive_index: archive_record = gsa_archive_index[cn[:-1]]
	if archive_record is not None: record["_archive"] = archive_record
	
	folder = None
	if "089" in record:
		folder = ("Reproduktionen", )
	elif "11z" in record:
		folder = wa_references[tuple(record["11z"].split(","))]	
	elif "08" in record:
		cn = gsa_cn(record["08"])
		if cn in paralip_mappings_index:
			folder = ("Paralipomena", )
			record["_paralipomenon"] = paralip_mappings_index[cn]
		else:
			for p_cn in paralip_mappings_index:
				if p_cn[:len(cn)] == cn:
					folder = ("Paralipomena", )
					record["_paralipomenon"] = paralip_mappings_index[p_cn]
	
	if folder is None: folder = ("Verschiedenes", )

	if folder in metadata_struct:
		metadata_struct[folder].append(record)
	else:
		metadata_struct[folder] = [ record ]
	
	#fields = record.keys()
	#fields.sort()
	#for field in fields: 
		#field_descr = (field in gsa_field_descrs) and gsa_field_descrs[field] or field
		#print field_descr, ":"
		#print record[field]
		#print
	#print "".rjust(70, "-")
print "\n".join(map(lambda x: repr(x) + " :: " + str(len(metadata_struct[x])), metadata_struct.keys()))