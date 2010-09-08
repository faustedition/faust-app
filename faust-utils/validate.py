#!/usr/bin/env python

import re
import shlex
import subprocess
import lxml.etree
import faust

validation_command = "java -jar '%s' '%s'" % (faust.config.get("validate", "jing"), faust.config.get("validate", "schema"))
validation_msg_re = re.compile(r'^(.+?):(\d+):(\d+):(.+?)$', re.MULTILINE)

validation_queue_max_length = 100
validation_queue = []

validation_report = dict()

def validate(last=False):
	"""Validates the content of the queue by calling Jing and parsing its output"""
	global validation_queue
	if not last and len(validation_queue) < validation_queue_max_length: return

	validation = subprocess.Popen(shlex.split(validation_command) + validation_queue, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
	validation_result = validation.communicate()[0] or ""
	for msg_match in validation_msg_re.finditer(validation_result):
		xml_file, line, column, msg = msg_match.groups()
		
		xml_file = faust.relative_path(xml_file)
		error_msg = "[%s:%s] %s" % (line, column, msg)
		
		if xml_file in validation_report: 
			validation_report[xml_file].append(error_msg)
		else:
			validation_report[xml_file] = [error_msg]

	validation_queue = []
	
# Validate all XML documents
for xml_file in faust.xml_files():
	if faust.is_tei_document(xml_file):
		validation_queue.append(xml_file)
		validate()
validate(True)

# Generate validation report
if len(validation_report) > 0:
	report = ""
	xml_url = faust.config.get("xml", "url")
	xml_files = validation_report.keys()
	xml_files.sort()
	for xml_file in xml_files:
		report += ((" " + xml_url + xml_file).rjust(78, "=") + "\n\n")
		report += ("\n".join(validation_report[xml_file]) + "\n\n")
		report += ("".rjust(78, "=") + "\n\n")
	faust.send_report("TEI-P5 Validation Errors", report)
