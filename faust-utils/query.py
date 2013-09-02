#!/usr/bin/env python
# coding=UTF-8
#
# Run XPath queries over all files
#

import sys, faust, lxml.etree, re, os.path

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

def list_matches (files, xpath):
	''' Lists tuples of the form [file, [match1, match2,...]]'''
	def matches_in_file (file):
		try:
			xml = lxml.etree.parse(file)
			return [file, faust.xpath(xpath)(xml)]
		except lxml.etree.XMLSyntaxError:
			sys.stderr.write("XML syntax error: " + file + "\n")
	return map(matches_in_file, files)

def show_matches (matches_in_files):
	for (file, matches) in [mf for mf in matches_in_files if mf]:
		if matches:
			print file
			for match in matches:
				print " " + str(match.attrib.get('n'))

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
			results = [result.tag for result in faust.xpath(xpath)(xml)]
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

def documentary_by_name():
	textual = re.compile(r"transcript/.*/(.*)/\1.xml")
	def d_b_n(file):
		rel = faust.relative_path(file)
		return not textual.match(rel)
	return filter(d_b_n, faust.transcript_files())


if __name__ == "__main__":

	# for f in non_wellformed(faust.transcript_files()):
	# 	print f
	# print_statistics()
	# find all manuscripts with red ink
	# for f in matches(faust.transcript_files(), "//tei:handShift[contains(@new, '_tr')] | //*[contains(@hand,'_tr')]"):
		# print f
	# for f in matches(faust.transcript_files(), 
	#		 """//tei:choice/text()[contains(., ' ') or contains(., '\t') or contains(., '\n')]"""):	print f
	# for f in matches(faust.transcript_files(), u"//text()[contains(.,'\x84') or contains(.,'\x93')]"):	print f
	# for val in unique_values (faust.transcript_files(), "//tei:facsimile/tei:graphic/@url"): print val
	# for f in matches(faust.transcript_files(), "count(//tei:facsimile/tei:graphic/@url) > 1"): print f
	# for f in matches(faust.transcript_files(), "//tei:gap[(not(@unit='chars') or not(@quantity)) and not(./ancestor::tei:rdg)]"): print f
	# not_available_xp = "not (" + kodiert_xp + " or " + encoded_xp + " or " + deleatur_xp +  " )"
	# for f in matches(faust.transcript_files(), not_available_xp):	print f
	# unencoded =  matches(faust.transcript_files(), "not( " + encoded_xp + " )")
	# for f in unencoded: print f
	#for f in documentary_by_name(): print f

	#	for f in matches(faust.transcript_files(), "//tei:change[(contains(@when, '2011-06-') or contains(@when, '2011-05-')) and contains(@who, 'bruening')]"):	print f

	# for f in matches(faust.transcript_files(), "//tei:zone[@type='main' and @rotate and @rotate != '180']"): print f

	# for f in matches(faust.transcript_files(), "//tei:text[not(.//text() or //tei:div[@type='template' or .//comment()])]"): print f

	# for f in matches(faust.transcript_files(), "//ge:document//text()[normalize-space(.) != ''] and not(" + encoded_xp + ")"): print f
	# for f in unique_values (faust.transcript_files(), "//tei:change/@who"): print f

# === hilfskraefte ===

	# names = ['mohr', 'sievert', 'badura', 'Sievert', 'arnold', 'blaschko', 'bethe']
	# for f in matches (faust.transcript_files(), "//tei:change/@who"):
	# 	xml = lxml.etree.parse(f)
	# 	changes = faust.xpath('//tei:change[@who]')(xml)
	# 	for change in changes:
	# 		if change.get('who') in names:
	# 			print change.get('when'), ' ', f




# ==== encoding ordered by date ====
	# for m in list_matches(faust.transcript_files(), "//tei:TEI/tei:teiHeader/tei:revisionDesc/tei:change[normalize-space(text())='encoded']/@when"):
	# 	if m and m[1]:
	# 		print m[1][-1],
	# 		print m[0]



# ==== EIGENHAENDIGE HANDSCHRIFTEN ====
	# eigenhaendig = 0
	# schreiber = 0
	# kA = 0
	# for m in list_matches(faust.files_in("document/"), "//f:materialUnit/f:metadataImport/f:archiveDatabase/f:schrift/text()"):
	# 	print m[0],
	# 	pages = list_matches([m[0]], "//f:materialUnit/f:materialUnit[@type='page']")[0][1]
	# 	if m[1]:
	# 		value = m[1][0].encode('utf8')
	# 		if ('egh' in value):
	# 			eigenhaendig = eigenhaendig + len(pages)
				
	# 		else:
	# 			schreiber = schreiber + len(pages)
	# 		print value,
	# 	else:
	# 		kA = kA + len(pages)
	# 		print 'keine_Angabe',
	# 	print len(pages)

	# print
	# print '(u.A.) eigenhaendig: ', eigenhaendig
	# print 'nicht eigenhaendig: ', schreiber
	# print 'keine Angabe: ', kA
# ========


# ==== ENCODING STATUS BY ACT ====
	# encoded_transcripts = matches(faust.transcript_files(), encoded_xp)
	# deleatur_transcripts = matches(faust.transcript_files(), deleatur_xp)

	# assigned = []
	# mapping = {}
	# for phrase in ['I ', 'II ', 'III ', 'IV ', 'V ']:
	# 	for f in matches(faust.transcript_files(),
	# 					 "//tei:altIdentifier[contains(@type, 'edition')]/tei:idno[starts-with(.,'"
	# 					 + phrase + "')]"):
	# 		gsanumber = re.search(r'[0-9][0-9][0-9][0-9][0-9][0-9]', f)
	# 		if gsanumber:
	# 			mapping [f[gsanumber.start():gsanumber.end()]] = phrase
	# 	print
	# 	print 'Akt ', phrase
	# 	print
	# 	for f in faust.transcript_files():
	# 		gsanumber = re.search(r'[0-9][0-9][0-9][0-9][0-9][0-9]', f)
	# 		if gsanumber:
	# 			if mapping.get(f[gsanumber.start():gsanumber.end()], '') == phrase:
	# 				if (not f in encoded_transcripts) and (not f in deleatur_transcripts):
	# 					if not f in assigned:
	# 						print '   ',  f
	# 						assigned.append(f)
	# print
	# print 'Nicht zugeordnet'
	# print
	# for f in faust.transcript_files():
	# 		if not f in assigned:
	# 			if (not f in encoded_transcripts) and (not f in deleatur_transcripts): 
	# 				print '   ',  f
			
# =========

# ==== ARCHIVAL UNITS ====


	# arch_units = matches(faust.files_in('document/'), '//f:materialUnit[@type="archival_unit"]')
	# # xml = lxml.etree.parse(file)
	# # return faust.xpath(xpath)(xml)


	# pgs = matches(faust.files_in('document/'), '//f:materialUnit')
	
	# print len(arch_units)
	# # print len(pgs)
	
# =========

# ==== look for lines with soon, then instant revision ====
 	# for (file, matches) in list_matches(faust.transcript_files(), '//tei:p[./descendant::*[@f:revType="soon"]/following-sibling::*[@f:revType="instant"]]'):
	# 	if matches:
	# 		print file
	# 		# for match in matches:
	# 			# print " " + str(match)
# =======			
	
	# in which manuscripts are the line numbers not in final order (schroer)
	# show_matches(list_matches(faust.transcript_files(), "//tei:l[number(@n) <  number(./preceding::tei:l[1]/@n)]"))
#	for val in unique_values (faust.transcript_files(), "//ge:document//*"): print val

# === find the subset of  manuscripts that doesn't contain certain markup

	bad_markup = [
		"//*[@rend='font-big']",
		"//*[@rend='font-small']",
		"//@f:pos",
		"//ge:patch",
		"//tei:reg",
		"//tei:sic",
		"//tei:corr",
		"//tei:g[@ref='#parenthesis_left']",
		"//tei:g[@ref='#parenthesis_right']",
		"//tei:g[@ref='#truncation']",
		"//tei:g[@ref='#ditto-quote']",
		"//tei:g[@ref='#ditto-line']",
		"//tei:g[@ref='#g_break']",
		"//tei:g[@ref='#g_transp_1']",
		"//tei:g[@ref='#g_transp_2']",
		"//tei:g[@ref='#g_transp_3']",
		"//tei:g[@ref='#g_transp_4']",
		"//tei:g[@ref='#g_transp_5']",
		"//tei:g[@ref='#g_transp_6']",
		"//tei:g[@ref='#g_transp_7']",
		"//tei:expan",
		"//tei:ex",
		"//*[@rend='underline_double']",
		"//*[@rend='underline_triple']",
		"//*[@rend='underline_quadruple']",
		"//*[@rend='']",
		"//*[@rend='frame']",
		"//*[@rend='overline']",
		"//*[@rend='vertical']",
		"//tei:g[@ref='']",
		"//f:st//f:st",
		"//f:st[@rend='erase']",
		"//tei:hi[@rend='underdots']",
		"//f:overw",
		"//ge:rewrite",
		#fixierung mit aenderung redundant
		# "//tei:seg[@type='between']", funktioniert schon
		"//tei:seg[@rend='inbetween']",
		"//f:grLine",
		"//f:grBrace",
		"//f:ins//f:ins",
		"//comment()[contains(.,'S-Linie')]",
		"//tei:gap[@atLeast or @atMost]",
		"//tei:gap[not(@unit='chars')]",
		"//tei:damage",
		"//*[@cert='low']",
		"//tei:certainty",
		"//tei:supplied",
		# u"//text()[contains(.,'\u0029')]",
		# u"//text()[contains(.,'\u0026')]",
		u"//text()[contains(.,'\u23BC')]",
		u"//text()[contains(.,'\u23D1')]",
		u"//text()[contains(.,'\u0301')]",
		u"//text()[contains(.,'\u2114')]",
		u"//text()[contains(.,'\u0370')]",
		u"//text()[contains(.,'\u0371')]",
		u"//text()[contains(.,'\u0372')]",
		u"//text()[contains(.,'\u0373')]",
		u"//text()[contains(.,'\u0374')]",
		u"//text()[contains(.,'\u0375')]",
		u"//text()[contains(.,'\u0376')]",
		u"//text()[contains(.,'\u0377')]",
		u"//text()[contains(.,'\u037A')]",
		u"//text()[contains(.,'\u037B')]",
		u"//text()[contains(.,'\u037C')]",
		u"//text()[contains(.,'\u037D')]",
		u"//text()[contains(.,'\u037E')]",
		u"//text()[contains(.,'\u037F')]",
		# ... TODO griechische Zeichen ausgelassen
		u"//text()[contains(.,'\u203f')]", #8255
		u"//text()[contains(.,'\u2044')]", #8260
		u"//text()[contains(.,'\u204c')]", #8268
		u"//text()[contains(.,'\u00b7')]", #183
		u"//text()[contains(.,'\u231c')]", #8988
		u"//text()[contains(.,'\u231d')]", #8989
		u"//text()[contains(.,'\u231e')]", #8990
		u"//text()[contains(.,'\u231f')]", #8991
		u"//text()[contains(.,'\u2609')]", #9737
		u"//text()[contains(.,'\u263d')]", #9789
		u"//text()[contains(.,'\u0a50')]", #2640
		u"//text()[contains(.,'\u2e13')]", #11795
		u"//text()[contains(.,'\u2713')]", #10003
		u"//text()[contains(.,'\u002B')]",
		u"//text()[contains(.,'\u274C')]",
		# u"//text()[contains(.,'\u00bc')]", # 188
		# u"//text()[contains(.,'\u00bd')]", # 189
		# u"//text()[contains(.,'\u00be')]", # 190
		u"//text()[contains(.,'\u0391')]", # 913
		u"//text()[contains(.,'\u0392')]", # 914
		u"//text()[contains(.,'\u0393')]", # 915
		u"//text()[contains(.,'\u0394')]", # 916
		u"//text()[contains(.,'\u03b1')]", # 945
		u"//text()[contains(.,'\u03b2')]", # 946
		u"//text()[contains(.,'\u02e0')]", # 736
		u"//text()[contains(.,'\u03b4')]", # 948
		]
	bad_markup_disjunction = ' | '.join(bad_markup)
	all_documents = set(map (os.path.dirname, faust.transcript_files()))
	print bad_markup_disjunction
	bad_documents = set(map (os.path.dirname, matches(faust.transcript_files(), bad_markup_disjunction)))
	
	for d in all_documents.difference(bad_documents):
	 	print '/'.join(d.split('/')[-2:])
