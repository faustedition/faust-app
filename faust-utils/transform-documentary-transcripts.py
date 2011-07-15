#!/usr/bin/env python

import faust
import query
import lxml.etree
import os.path

text_xp = faust.xpath("//tei:text")

for f in query.matches (query.documentary_by_name(), "//tei:text and not(//ge:document)"):
	relpath = faust.relative_path(f)
	
	xml = lxml.etree.parse(f)
	text = text_xp(xml)[0]

	gedocument = lxml.etree.Element(faust.ns("ge") + "document", nsmap=faust.namespaces)
	surface = lxml.etree.Element(faust.ns("tei") + "surface")
	gedocument.append(surface)
	zone = lxml.etree.Element(faust.ns("tei") + "zone")
	zone.set("type", "main")
	surface.append(zone)

	text.addprevious(gedocument)

	out = os.path.join("/tmp/faust/" + relpath)
	outdir = os.path.dirname(out)
	try:
		os.makedirs (outdir)
	except:
		pass
	xml.write(out, encoding="UTF-8")



