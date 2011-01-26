#!/usr/bin/env python
#
# Report on the transcript status as specified under
#
# https://faustedition.uni-wuerzburg.de/wiki/index.php/Stand_der_Transkription
#

import sys

import lxml.etree

import faust

# XPath expression for extracting the revision history from TEI documents
change_xp = faust.xpath("//tei:teiHeader//tei:revisionDesc/tei:change")

def count():
	# status counters
	status_dict = {"encoded": 0, "proof-read": 0, "published": 0 }

	status_candidates = status_dict.keys()
	status_candidates.sort()

	status_unknown = 0

	# iterate over all TEI documents
	for xml_file in faust.transcript_files():
		status = None
		try:
			if faust.is_tei_document(xml_file):
				xml = lxml.etree.parse(xml_file)

				# iterate over all change records, searching for a status remark and select the last one
				for change in change_xp(xml):
					change_str = lxml.etree.tostring(change).lower()
					for candidate in status_candidates:
						if candidate in change_str: status = candidate
		except IOError:
			sys.stderr.write("I/O error while extracting status from " + xml_file + "\n")
		except lxml.etree.XMLSyntaxError:
			sys.stderr.write("XML error while extracting status from " + xml_file + "\n")

		if status is None:
			# no status given
			status_unknown += 1
		else:
			# increment relevant status entry
			status_dict[status] += 1
	return status_dict, status_unknown

if __name__ == "__main__":
	status_dict, status_unknown = count()
	status_candidates = status_dict.keys()	
	# generate and send report
	report = "".rjust(40, "=") + "\n"

	for status in status_candidates:
		report += status + str(status_dict[status]).rjust(40 - len(status)) + "\n"
		report += "".rjust(40, "-") + "\n"

	report += "n/a" + str(status_unknown).rjust(40 - len("n/a")) + "\n"
	report += "".rjust(40, "=") + "\n"

	faust.send_report("Stand der Transkription", report)
