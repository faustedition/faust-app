#!/usr/bin/env python
# coding=UTF-8
#
# Compare two  directories of TEI files,  ignoring whitespace. Replace
# tei:text and ge:document elements if nothing significant has changed


from __future__ import print_function
import io
import string
import os
import sys
import lxml.etree
import faust
import copy
import rev_desc

txt_xp = faust.xpath("//tei:TEI/tei:text")
doc_xp = faust.xpath("//tei:TEI/ge:document")
body_xp = faust.xpath("//tei:TEI/tei:text/tei:body")

#automatically generated templates
template_xp = faust.xpath("//tei:TEI/tei:text/tei:body/tei:div[@type='template']")



# False for a dry run
replace_new = True

def compare_streams(one, two):
	'''Decides if two streams are equal, ignoring whitespace'''

	char_1 = ' '
	char_2 = ' '
	
	while char_1 or char_2:
		while char_1 and string.whitespace.find(char_1) >= 0:
			char_1 = one.read(1)

		while char_2 and string.whitespace.find(char_2) >= 0:
			char_2 = two.read(1)
			
		if char_1 != char_2:
			print()
			print('*** ONE ***')
			print(char_1.encode('utf8'), end='')
			print(one.read(20).encode('utf8'))
			print('*** TWO ***')
			print(char_2.encode('utf8'), end='')
			print(two.read(20).encode('utf8'))
			return False

		char_1 = one.read(1)
		char_2 = two.read(1)

	return True

def compare_elements(one, two):
	'''Decides if two lists of elements are equal, ignoring whitespace'''
	if len(one) == len(two) == 1:

		lxml.etree.ElementTree(one[0]).write_c14n('/tmp/compare_1')
		lxml.etree.ElementTree(two[0]).write_c14n('/tmp/compare_2')

		onestream = io.open('/tmp/compare_1')
		twostream = io.open('/tmp/compare_2')

		result = compare_streams(onestream, twostream)
		print("*", result, "*", end='')
		return result

	elif not len(one) == len(two) == 0:
		print("# of elements : ", len(one), ", ", len(two), end='')

	return False

def compare_dirs(one, two):
	'''Compares two directories, ignoring whitespace.

	Replaces the tei:text and ge:document elements files in one by
	those of files in two if equal'''

	for dir in os.walk(one):
		for file in dir[2]:
			if file.endswith('.xml'):
				path_1 = os.path.join(dir[0], file)
				relpath =  os.path.relpath(path_1, one)
				path_2 = os.path.join(two, relpath)
				print(relpath, '\t', end='')
				
				if os.path.isfile(path_2):
					try:
						
						xml_1 = lxml.etree.parse(path_1)
						xml_2 = lxml.etree.parse(path_2)

						#remove templates from new xml
						templates = template_xp(xml_1)
						if templates:
							templates[0].getparent().remove(templates[0])

						print("tei:text   ", end='')
						txt_equal = compare_elements(txt_xp(xml_1), txt_xp(xml_2))
						print("\t", end='');

						print("ge:document   ", end='')
						doc_equal = compare_elements(doc_xp(xml_1), doc_xp(xml_2))
						print("\t", end='')
						
						if replace_new:
							if txt_equal:
								replace(txt_xp(xml_1)[0], txt_xp(xml_2)[0])
							if doc_equal:
								replace(doc_xp(xml_1)[0], doc_xp(xml_2)[0])
							if templates:
								body_xp(xml_1)[0].append(copy.deepcopy(templates[0]))

							if txt_equal or doc_equal:
								rev_desc.add_change(xml_1, "system", "whitespace-restored")
								faust.tei_serialize(xml_1).write(path_1, encoding="UTF-8")


						if templates:
							print(" *t* ", end='')
						if txt_equal or doc_equal:
							print(" ***MODIFIED***", end='')
							
					except lxml.etree.XMLSyntaxError:
						print("XML syntax error", end='')
					except ValueError as e:
						print(e, end='');
				else:
					print ("not in dir2", end='')
				print()


def replace(node, with_node):
	'''Replaces a node with a deep copy of a node (from another document)'''
	node.getparent().replace(node, copy.deepcopy(with_node))



def canonicalize_dir(directory):
	'''Canonicalize all XML files in a directory'''

	for dir in os.walk(directory):
		for file in dir[2]:
			if file.endswith('.xml'):


# compare_dirs(
# 	"/Users/moz/d/faustedition/xml/transcript",
# 	"/Users/moz/d/faustedition/whitespace/2010-11-02_pre_whitespace_convert/transcript"
# 	)

