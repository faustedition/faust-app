#!/usr/bin/env python
#
# Adds template verses with genetic indices based on metadata import

import re
import sys
import lxml.etree

import faust

line_number_xp = faust.xpath("//f:field[f:key[@n='41v']]/f:value")
line_number_re = re.compile(r'^([0-9]+)[^0-9a-b\-]*-?([0-9]+)?')

for xml_file in faust.xml_files():
	try:
		rel_path = faust.relative_path(xml_file)
		if rel_path.startswith("document"):
			xml = lxml.etree.parse(xml_file)			
			for val in line_number_xp(xml):
				val_t = val.xpath("text()")[0]
				print val_t, "==>", repr(line_number_re.findall(val_t))
	except IOError:
		sys.stderr.write("I/O error while reading " + xml_file + "\n")
	except lxml.etree.XMLSyntaxError:
		sys.stderr.write("XML error while reading " + xml_file + "\n")
		