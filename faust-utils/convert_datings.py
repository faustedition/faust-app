#!/usr/bin/env python
#
# Convert manuscript datings in CSV form to xml format
#

import csv
import sys
import string
# import lxml.etree

csvreader = csv.reader(sys.stdin)

print '<?xml version="1.0" encoding="UTF-8"?>\
<macrogenesis xmlns="http://www.faustedition.net/ns"\
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\
    xsi:schemaLocation="http://www.faustedition.net/ns https://faustedition.uni-wuerzburg.de/xml/schema/macrogenesis.xsd">'

for row in csvreader:
	if string.find(row[6], "5. Akt") >= 0:

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
				print '   <date',
				if from_date_given:
					print 'from="%04d-%02d-%02d"' % (int(from_year), int(from_month), int(from_day)),
				if to_date_given:
					print 'to="%04d-%02d-%02d"' % (int(to_year), int(to_month), int(to_day)),
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

				
			

print '</macrogenesis>'

sys.stdout.flush()
