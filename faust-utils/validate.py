#!/usr/bin/env python

import lxml.etree
import faust

schema = lxml.etree.parse(faust.config.get("xml", "schema"))
print "Schema parsed"

schema_validator = lxml.etree.RelaxNG(schema)
print "Schema validator built"

for xml_file in faust.xml_files():
	print xml_file
	lxml.etree.parse(xml_file)
