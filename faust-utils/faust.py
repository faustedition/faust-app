#!/usr/bin/env python

import ConfigParser
import os
import os.path

config = ConfigParser.ConfigParser()
config.read(['faust.ini', "local.ini"])

def xml_files():
	"""Returns paths of all XML documents in the edition"""
	xml_files = []
	for root, dirs, files in os.walk(config.get("xml", "dir")):		
		for f in files: 
			if f.endswith(".xml"): xml_files.append(os.path.join(root, f))
	return xml_files

