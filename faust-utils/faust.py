#!/usr/bin/env python

import ConfigParser
import email.mime.text
import os
import os.path

import lxml.etree

config = ConfigParser.ConfigParser()
config.read(['faust.ini', "local.ini"])

report_sender = "Faust-Edition <noreply@faustedition.net>"
report_recipients = ["Gregor Middell <gregor@middell.net>"]

def relative_path(xml_file):
	"""Returns the path of the given XML file relative to the base directory"""
	return os.path.relpath(xml_file, config.get("xml", "dir"))

def xml_files():
	"""Returns paths of all XML documents in the edition"""
	xml_files = []
	for root, dirs, files in os.walk(config.get("xml", "dir")):		
		for f in files: 
			if f.endswith(".xml"): xml_files.append(os.path.join(root, f))
	xml_files.sort()
	return xml_files

def is_tei_document(xml_file):
	"""Determines whether a XML file is a TEI document by checking the namespace of the first element encountered"""
	for event, element in lxml.etree.iterparse(xml_file):
		if element is None: continue
		return element.tag.startswith("{http://www.tei-c.org/ns/1.0}")

def send_report(subject, msg):
	if config.getboolean("mail", "enabled"):
		msg = email.mime.text.MIMEText(msg)
		msg["Subject"] = subject
		msg["From"] = report_sender
		msg["To"] = ", ".join(report_recipients)
		
		server = smtplib.SMTP('localhost')
		server.sendmail(report_sender, report_recipients, msg.as_string())
		server.quit()
	else:
		print "Subject:", subject
		print
		print msg
	