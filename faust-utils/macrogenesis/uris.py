import csv
import json
import logging
import re
import urllib2
from collections import defaultdict

from lxml import etree
import faust


class Inscription(object):

    def __init__(self, witness, inscription):
        self.witness = witness
        self.inscription = inscription
        self.known_inscription = inscription in witness.inscriptions
        self.known_witness = isinstance(witness, Witness)
        if self.known_inscription and self.known_witness:
            self.status = 'ok'
        elif self.known_witness:
            self.status = 'unknown-inscription'
        else:
            self.status = 'unknown'

    @property
    def uri(self):
        return "/".join([self.witness.uri.replace('faust://document/', 'faust://inscription/'), self.inscription])

    def __str__(self):
        return self.uri

    def __hash__(self):
        return hash(self.witness) ^ hash(self.inscription)

    def __eq__(self, other):
        if isinstance(other, Inscription):
            return self.witness == other.witness and self.inscription == other.inscription


class UnknownRef(object):

    def __init__(self, uri):
        self.uri = uri
        self.status = "unknown"

    def __str__(self):
        return self.uri

    def __hash__(self):
        return hash(self.uri)

    def __eq__(self, other):
        if isinstance(other, UnknownRef):
            return self.uri == other.uri
        else:
            return str(self) == str(other)


class Witness(object):
    database = {}

    def __init__(self, doc_record):
        if isinstance(doc_record, dict):
            self.__dict__.update(doc_record)
            self.status = "ok"
        else:
            raise TypeError('doc_record must be a mapping, not a ' + str(type(doc_record)))

    def uris(self):
        yield self.uri
        if hasattr(self, 'other_sigils'):
            for uri in self.other_sigils:
                yield uri
                if u'/wa_faust/' in uri:
                    yield uri.replace('/wa_faust/', '/wa/')

    @classmethod
    def _load_database(cls,
                       url='http://dev.digital-humanities.de/ci/job/faust-gen-fast/lastSuccessfulBuild/artifact/target/uris.json'):
        sigil_json = urllib2.urlopen(url)
        sigil_data = json.load(sigil_json)
        sigil_json.close()
        for doc in sigil_data:
            wit = cls(doc)
            for uri in wit.uris():
                cls.database[uri] = wit

        for ref, uri in sorted(cls.database.items()):
            print ref, uri

    @classmethod
    def get(cls, uri):
        if not cls.database:
            cls._load_database()

        if uri in cls.database:
            return cls.database[uri]

        if uri.startswith('faust://inscription'):
            try:
                system, sigil, inscription = re.match('^faust://inscription/(.*)/(.*)/(.*)', uri).groups()
                base = "/".join(['faust://document', system, inscription])
                if base in cls.database:
                    wit = cls.database[base]
                    return Inscription(wit, inscription)
            except AttributeError:
                logging.error("Broken inscription uri: %s", uri)

        logging.warning('Unknown reference: %s', uri)
        return UnknownRef(uri)

    def __hash__(self):
        return hash(self.uri)

    def __eq__(self, other):
        if isinstance(other, Witness):
            return self.uri == other.uri
        return str(self) == str(other)

    def __str__(self):
        return self.uri


def _collect_wits():
    items = defaultdict(list)  # type: Dict[Union[Witness, Inscription, UnknownRef], List[Tuple[str, int]]]
    for macrogenetic_file in faust.macrogenesis_files():
        tree = etree.parse(macrogenetic_file)  # type: etree._ElementTree
        for element in tree.xpath('//f:item', namespaces=faust.namespaces):  # type: etree._Element
            uri = element.get('uri')
            wit = Witness.get(uri)
            items[wit].append((macrogenetic_file, element.sourceline))
    return items


def _report_wits(wits, output_csv='witness-usage.csv'):
    with open(output_csv, "wt") as reportfile:
        table = csv.writer(reportfile)
        table.writerow(['URI', 'Status', 'Vorkommen'])
        for wit in sorted(wits, key=str):
            table.writerow([
                wit.uri,
                wit.status,
                ", ".join([file + ":" + str(line) for file, line in wits[wit]])
            ])
        for wit in Witness.database.values():
            if wit not in wits:
                table.writerow([
                    wit.uri,
                    "no-data",
                    ""
                ])

if __name__ == '__main__':
    wits = _collect_wits()
    _report_wits(wits)
