#!/usr/bin/env python
#
# Convert manuscript datings in CSV form to xml format
#

import csv
import sys
import string
import datetime
# import lxml.etree



def header(f):
	sys.stdout = f
	
	print '<?xml version="1.0" encoding="UTF-8"?>\
<macrogenesis xmlns="http://www.faustedition.net/ns"\
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\
    xsi:schemaLocation="http://www.faustedition.net/ns https://faustedition.uni-wuerzburg.de/xml/schema/macrogenesis.xsd">'



def process_row(row, f):
	sys.stdout = f
	new_gsa_sigil = string.strip(row[0])
	old_gsa_sigil = string.strip(row[1])
	foreign_sigil = string.strip(row[2])
	sigil = string.strip(row[3])
	other_sigil = string.strip(row[4])

	# Paralipomenon, Textzuordnung, Verse entfallen
	
	abs_date = string.strip(row[8])
	entstehungszeit = string.strip(row[9])
	rel_date = string.strip(row[10])

	from_day = string.strip(row[11])
	from_month = string.strip(row[12])
	from_year = string.strip(row[13])

	# 'formal' entfaellt

	to_day = string.strip(row[15])
	to_month = string.strip(row[16])
	to_year = string.strip(row[17])

	# 'formal' entfaellt
		
	argument = string.strip(row[19])
	source = string.strip(row[20])
	indiz = string.strip(row[21])
	ueberlieferte_datierung = string.strip(row[22])
	comment = string.strip(row[23])

	print '  <!-- %s / %s / %s / %s / %s -->' % (new_gsa_sigil, old_gsa_sigil, foreign_sigil, sigil, other_sigil) 
	if len(entstehungszeit) > 0:
		print '   <!-- Entstehungszeit: %s -->' % entstehungszeit
	if len(ueberlieferte_datierung) > 0:
		print '   <!-- Ueberlieferte Datierung: %s -->' % ueberlieferte_datierung
		
	id = other_sigil if len(other_sigil) > 0 else sigil

	composed_comment = '%s. %s. %s.' % (argument, indiz, comment)

	# absolute dating
		
	if len(abs_date) > 0:
		print '   <!-- %s -->' %  abs_date
		from_date_given = from_day and from_month and from_year
		to_date_given = to_day and to_month and to_year
		if from_date_given or to_date_given:
			one_day = datetime.timedelta(1)


			



			print '   <date',
			if from_date_given:
				datetime_from_date = datetime.datetime(int(from_year), int(from_month), int(from_day))
				datetime_from_date = datetime_from_date + one_day
				print 'notBefore="%04d-%02d-%02d"' % (datetime_from_date.year, datetime_from_date.month, datetime_from_date.day),
			if to_date_given:
				datetime_to_date = datetime.datetime(int(to_year), int(to_month), int(to_day))
				datetime_to_date = datetime_to_date - one_day
				print 'notAfter="%04d-%02d-%02d"' % (datetime_to_date.year, datetime_to_date.month, datetime_to_date.day),
			print '>'
			print '      <comment>%s</comment>' %  (composed_comment)
			print '      %s%s%s' %  ('<source uri="', source, '"/>')
			print '      %s%s%s' %  ('<item uri="', id, '"/>')
			print '   </date>'

		
	# relative dating
	if len(rel_date) > 0:
		def print_rel_header (rel_name):
			print '   <relation name="%s">' % rel_name
			print '      %s%s%s' %  ('<source uri="', source, '"/>')
			print '      <comment>%s</comment>' %  (composed_comment)				
		for rel in [string.strip(x) for x in string.split(rel_date, ';')]:
			if (string.find(rel.lower(), 'vor') == 0):
				print_rel_header ("temp-pre")
				print '      %s%s%s' %  ('<item uri="', id, '"/>')
				print '      %s%s%s' %  ('<item uri="', string.strip(rel[3:]), '"/>')
				print '   </relation>'
			elif (string.find(rel.lower(), 'nach') == 0):
				print_rel_header ("temp-pre")
				print '      %s%s%s' %  ('<item uri="', string.strip(rel[4:]), '"/>')
				print '      %s%s%s' %  ('<item uri="', id, '"/>')
				print '   </relation>'
			elif (string.find(rel.lower(), 'mit') == 0):
				print_rel_header ("temp-syn")
				print '      %s%s%s' %  ('<item uri="', id, '"/>')
				print '      %s%s%s' %  ('<item uri="', string.strip(rel[4:]), '"/>')
				print '   </relation>'
			elif (string.find(rel.lower(), 'um') == 0):
				print_rel_header ("temp-about")
				print '      %s%s%s' %  ('<item uri="', id, '"/>')
				print '      %s%s%s' %  ('<item uri="', string.strip(rel[2:]), '"/>')
				print '   </relation>'
			else:
				print '<!-- %s -->' % (rel)				

				
			


def finish(f):
	sys.stdout = f
	print '</macrogenesis>'
	f.close()
	



csvreader = csv.reader(sys.stdin)

# f_1 = open('i.xml', 'w')
# f_2 = open('ii.xml', 'w')
# f_3 = open('iii.xml', 'w')
# f_4 = open('iv.xml', 'w')
# f_5 = open('v.xml', 'w')
# f_rest = open('rest.xml', 'w');

# files = [f_1, f_2, f_3, f_4, f_5, f_rest]
# for f in files
#	header(f)

header(sys.stdout)

# skip the headings line
csvreader.next()

for row in csvreader:
#	if string.find(row[6], "5. Akt") >= 0:
		process_row(row, sys.stdout)

finish(sys.stdout)

#for f in files:
#	finish(f)
