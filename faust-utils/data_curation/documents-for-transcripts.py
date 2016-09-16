import faust, re, sys

cp = faust.config.get("deploy", "base_url")
archival_document_path = 'document/archival/'
xml_suffix = '.xml'
xml_dir = faust.config.get("xml", "dir")


def extract_gsa_number(path):
    match = re.search(r'[0-9][0-9][0-9][0-9][0-9][0-9]', path)
    return match.group() if match else None

def find_document_for_gsa_number(gsa_number):
    for document in faust.document_files():
        if gsa_number in document:
            return document



for line in open (sys.argv[1]):
    gsa_number = extract_gsa_number(line)
    if gsa_number:
        relpath = find_document_for_gsa_number(gsa_number)[len(xml_dir) + 1:]
        print "   '" + cp + relpath + "',"
    else:
        print "   '" + cp + archival_document_path + line[:-1] + xml_suffix + "',"
    
