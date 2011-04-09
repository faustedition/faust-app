#!/usr/bin/env python
# coding=UTF-8
#
# Run XPath queries over all files
#

import sys
import faust
import lxml.etree

kodiert_xp = "//tei:TEI/tei:teiHeader/tei:revisionDesc/tei:change[normalize-space(text())='kodiert']"
encoded_xp = "//tei:TEI/tei:teiHeader/tei:revisionDesc/tei:change[normalize-space(text())='encoded']"
kodiert_and_encoded_xp = kodiert_xp + " and " + encoded_xp
deleatur_xp = "//tei:TEI/tei:teiHeader/tei:revisionDesc/tei:change[normalize-space(text())='deleatur']"

def matches (files, xpath):
	''' List files that have matches for xpath.'''
	def does_match (file):
		try:
			xml = lxml.etree.parse(file)
			return faust.xpath(xpath)(xml)
		except lxml.etree.XMLSyntaxError:
			sys.stderr.write("XML syntax error: " + file + "\n")
	return filter(does_match, files)

def non_wellformed (files):
	''' List non-wellformed xml files. '''
	def is_not_well (file):
		try:
			xml = lxml.etree.parse(file)
		except lxml.etree.XMLSyntaxError as e:
			return True
		return False
	return filter(is_not_well, files)

def unique_values(files, xpath):
	''' List all unique values for matches of an xpath.'''
	unique = set()
	for f in files:
		try:
			xml = lxml.etree.parse(f)
			results = faust.xpath(xpath)(xml)
			unique = unique.union(results)
		except lxml.etree.XMLSyntaxError:
			sys.stderr.write("XML syntax error: " + f + "\n")
	return unique

def print_statistics():
	''' Print the result of some queries. '''
	kodiert =  matches(faust.transcript_files(), kodiert_xp)
	encoded =  matches(faust.transcript_files(), encoded_xp)
	kodiert_and_encoded =  matches(faust.transcript_files(), kodiert_and_encoded_xp)

	print "kodiert: ", len(kodiert)
	print "encoded: ", len(encoded)
	print "kodiert und encoded: ", len(kodiert_and_encoded)

def invalid_facsimile_links():
	''' Print all files with invalid facsimile links'''
	faust_facsimiles = faust.facsimiles()
	def facs_invalid(file):
		xml = lxml.etree.parse(file)
		urls = faust.xpath("//tei:facsimile/tei:graphic/@url")(xml)
		for url in urls:
			if url in faust_facsimiles: return True
		return False
	return filter(facs_invalid, faust.transcript_files())


if __name__ == "__main__":

	# for f in non_wellformed(faust.transcript_files()):
	# 	print f

	# print_statistics()

	# find all manuscripts with red ink
	# for f in matches(faust.transcript_files(), "//tei:handShift[contains(@new, '_tr')] | //*[contains(@hand,'_tr')]"):
	# 	print f
	# for f in matches(faust.transcript_files(), "//ge:undo"):	print f
	# for val in unique_values (faust.transcript_files(), "//tei:facsimile/tei:graphic/@url"): print val
	# for f in matches(faust.transcript_files(), "count(//tei:facsimile/tei:graphic/@url) > 1"): print f
	# not_available_xp = "not (" + kodiert_xp + " or " + encoded_xp + " or " + deleatur_xp +  " )"
	# for f in matches(faust.transcript_files(), not_available_xp):	print f
	unencoded =  matches(faust.transcript_files(), "not( " + encoded_xp + " )")
	for f in unencoded: print f



