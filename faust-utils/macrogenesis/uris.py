#!/usr/bin/env python
import codecs
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
        self.known_witness = isinstance(witness, Witness)
        if self.known_witness:
            self.known_inscription = inscription in getattr(witness, 'inscriptions', {})
            if self.known_inscription:
                self.status = 'ok'
            else:
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
            return self is other


class AmbiguousRef(object):

    def __init__(self, uri, wits):
        self.uri = uri
        self.witnesses = frozenset(wits)
        self.status = 'ambiguous: ' + ", ".join(str(wit) for wit in sorted(self.witnesses))

    def __hash__(self):
        return hash(self.uri)

    def __eq__(self, other):
        if isinstance(other, AmbiguousRef):
            return self.uri == other.uri
        return self is other

    def first(self):
        return sorted(self.witnesses, key=str)[0]

    def __add__(self, other):
        new_witnesses = {other} if isinstance(other, Witness) else set(other.witnesses)
        new_witnesses = new_witnesses.union(self.witnesses)
        return AmbiguousRef(self.uri, new_witnesses)

    def __str__(self):
        return self.uri


class Witness(object):
    database = {}

    def __init__(self, doc_record):
        if isinstance(doc_record, dict):
            self.__dict__.update(doc_record)
            self.status = "ok"
        else:
            raise TypeError('doc_record must be a mapping, not a ' + str(type(doc_record)))

    def uris(self):
        result = [self.uri]
        if hasattr(self, 'other_sigils'):
            for uri in self.other_sigils:
                result.append(uri)
                if u'/wa_faust/' in uri:
                    result.append(uri.replace('/wa_faust/', '/wa/'))
        if getattr(self, 'type', '') == 'print':
            result.extend([uri.replace('faust://document/', 'faust://print/') for uri in result])
        return result

    @classmethod
    def _load_database(cls,
                       url='http://dev.digital-humanities.de/ci/job/faust-gen-fast/lastSuccessfulBuild/artifact/target/uris.json'):
        sigil_json = urllib2.urlopen(url)
        sigil_data = json.load(sigil_json)
        sigil_json.close()
        cls.database = cls.build_database(sigil_data)

    @classmethod
    def build_database(cls, sigil_data):
        database = {}
        for doc in sigil_data:
            wit = cls(doc)
            for uri in wit.uris():
                if uri in database:
                    old_entry = database[uri]
                    logging.warning("URI %s is already in db for %s, adding %s", uri, old_entry, wit)
                    if isinstance(old_entry, AmbiguousRef):
                        database[uri] = old_entry + wit
                    else:
                        database[uri] = AmbiguousRef(uri, {database[uri], wit})
                else:
                    database[uri] = wit
        return database

    @classmethod
    def get(cls, uri, allow_duplicate=False):
        if not cls.database:
            cls._load_database()

        if uri in cls.database:
            result = cls.database[uri]
            if isinstance(result, AmbiguousRef):
                return result if allow_duplicate else result.first()
            else:
                return result

        if uri.startswith('faust://inscription'):
            match = re.match('faust://inscription/(.*)/(.*)/(.*)', uri)
            if match is not None:
                system, sigil, inscription = match.groups()
                base = "/".join(['faust://document', system, sigil])
                wit = cls.get(base)
                return Inscription(wit, inscription)

        logging.warning('Unknown reference: %s', uri)
        return UnknownRef(uri)

    def __hash__(self):
        return hash(self.uri)

    def __eq__(self, other):
        if isinstance(other, Witness):
            return self.uri == other.uri
        return str(self) == str(other)

    def __str__(self):
        return self.sigil_t


def _collect_wits():
    items = defaultdict(list)  # type: Dict[Union[Witness, Inscription, UnknownRef], List[Tuple[str, int]]]
    for macrogenetic_file in faust.macrogenesis_files():
        tree = etree.parse(macrogenetic_file)  # type: etree._ElementTree
        for element in tree.xpath('//f:item', namespaces=faust.namespaces):  # type: etree._Element
            uri = element.get('uri')
            wit = Witness.get(uri)
            items[wit].append((macrogenetic_file.split('macrogenesis/')[-1], element.sourceline))
    return items


def _report_wits(wits, output_csv='witness-usage.csv'):
    with codecs.open(output_csv, "wt", encoding='utf-8') as reportfile:
        table = csv.writer(reportfile)
        table.writerow(['URI', 'Status', 'Vorkommen'])
        for wit in sorted(wits, key=str):
            table.writerow([
                str(wit),
                wit.status,
                ", ".join([file + ":" + str(line) for file, line in wits[wit]])
            ])
        for wit in sorted(set(Witness.database.values()), key=str):
            if wit not in wits:
                table.writerow([
                    str(wit),
                    "no-data",
                    ""
                ])


if __name__ == '__main__':
    wits = _collect_wits()
    _report_wits(wits)
