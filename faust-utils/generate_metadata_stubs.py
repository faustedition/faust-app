#!/usr/bin/env python
# coding=UTF-8
#
# Generate metadata stumps

import faust
import os.path
import correct_facsimile_links

documentary_transcripts = correct_facsimile_links.xml_names_from_facsimiles()

def stub(archive, unit, pages_snippet):
	return '''\
<f:materialUnit xmlns:tei="http://www.tei-c.org/ns/1.0"
                xmlns:ge="http://www.tei-c.org/ns/geneticEditions"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:f="http://www.faustedition.net/ns"
                type="archival_unit"
                xml:base="faust://xml/transcript/%s/%s/" transcript="%s.xml">
   <f:metadata>
      <f:archive>%s</f:archive>
      <f:callnumber>%s</f:callnumber>
      <f:waId>-</f:waId>
   </f:metadata>
%s
</f:materialUnit>''' % (archive, unit, unit, archive, unit, pages_snippet)

def write_file(content, path):
	dirname = os.path.dirname(path)
	if not os.path.exists(dirname):
		os.makedirs(dirname)
	f = open(path, 'w')
	f.write(content)
	f.close
	

def generate():
	t_dir = os.path.join(faust.xml_dir, 'transcript')
	archives = [name for name in os.listdir(t_dir)
				if os.path.isdir(os.path.join(t_dir, name))
				and not name == 'gsa']
	for archive in archives:
		a_dir = os.path.join(t_dir, archive)
		units = [name for name in os.listdir(a_dir)
				if os.path.isdir(os.path.join(a_dir, name))]
		for unit in units:
			u_dir = os.path.join(a_dir, unit)
			transcripts = [name for name in os.listdir(u_dir)
				if name.endswith('.xml')]
			pages_snippet = ''
			for transcript in transcripts:
				full_path = os.path.join(faust.xml_dir, 'transcript', archive, unit, transcript)
				# only include if documentary transcript
				if full_path in documentary_transcripts:
					pages_snippet +='      <f:materialUnit type="page" transcript="%s"/>\n' % transcript
			metadata_path = os.path.join(faust.xml_dir, 'document', 'archival', archive, unit + ".xml")
			print "writing ", metadata_path
			content = stub (archive, unit, pages_snippet)
			write_file (content, metadata_path)

if __name__ == "__main__":
	generate()

