#!/usr/bin/env python
import codecs
import csv
import json
import logging
import re
from functools import wraps
from urllib2 import urlopen
from collections import defaultdict, Counter
from operator import itemgetter

import pandas as pd
from lxml import etree

import faust


def call_recorder(function=None, argument_picker=None):
    """
    Decorator that records call / result counts.

    Args:
        argument_picker(Function): function that picks the interesting arguments
    """

    def decorator(fun):
        recorder = Counter()

        @wraps(fun)
        def wrapper(*args, **kwargs):
            relevant_args = args if argument_picker is None else argument_picker(args)
            result = fun(*args, **kwargs)
            recorder.update([(relevant_args, result)])
            return result

        wrapper.recorder = recorder
        return wrapper

    if callable(function) and argument_picker is None:
        return decorator(function)
    else:
        return decorator


class Inscription(object):

    def __init__(self, witness, inscription):
        self.witness = witness
        self.inscription = inscription
        self.known_witness = isinstance(witness, Witness)
        if self.known_witness:
            self.known_inscription = inscription in getattr(witness, 'inscriptions', {})
            if self.known_inscription:
                self.status = '(ok)'
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
    paralipomena = None

    def __init__(self, doc_record):
        if isinstance(doc_record, dict):
            self.__dict__.update(doc_record)
            self.status = '(ok)'
        else:
            raise TypeError('doc_record must be a mapping, not a ' + str(type(doc_record)))

    def uris(self):
        result = [self.uri]
        if hasattr(self, 'other_sigils'):
            for uri in self.other_sigils:
                uri = uri.replace('-', '_')
                result.append(uri)
                if u'/wa_faust/' in uri:
                    result.append(uri.replace('/wa_faust/', '/wa/'))
        if getattr(self, 'type', '') == 'print':
            result.extend([uri.replace('faust://document/', 'faust://print/') for uri in result])
        return result

    @classmethod
    def _load_database(cls,
                       url='http://dev.digital-humanities.de/ci/job/faust-gen-fast/lastSuccessfulBuild/artifact/target/uris.json'):
        sigil_json = urlopen(url)
        sigil_data = json.load(sigil_json)
        sigil_json.close()
        cls.database = cls.build_database(sigil_data)

    @classmethod
    def _load_paralipomena(cls,
                           url='http://dev.digital-humanities.de/ci/job/faust-gen-fast/lastSuccessfulBuild/artifact/target/www/data/paralipomena.js'):
        if cls.paralipomena is None:
            para_file = urlopen(url)
            json_str = '[' + ''.join(para_file.readlines()[1:])
            para_file.close()
            orig_para = json.loads(json_str, encoding='utf-8')
            cls.paralipomena = {p['n'].strip(): p for p in orig_para}

        return cls.paralipomena

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
    @call_recorder(argument_picker=itemgetter(1))
    def get(cls, uri, allow_duplicate=False):
        if not cls.database:
            cls._load_database()
            cls._load_paralipomena()

        orig_uri = uri
        uri = uri.replace('-', '_')

        if uri in cls.database:
            result = cls.database[uri]
            if isinstance(result, AmbiguousRef):
                return result if allow_duplicate else result.first()
            else:
                return result

        wa_pseudo_inscr = re.match('faust://(inscription|document)/wa/(\S+?)\.?alpha$', uri)
        if wa_pseudo_inscr is not None:
            docuri = 'faust://document/wa_faust/' + wa_pseudo_inscr.group(2)
            wit = cls.get(docuri)
            if isinstance(wit, Witness):
                return Inscription(wit, 'alpha')
            else:
                logging.warning('Could not fix WA pseudo inscription candidate %s (%s)', uri, wit)

        space_inscr = re.match('faust://(inscription|document)/(.*?)/(.*?)\s+(.*?)', uri)
        if space_inscr is not None:
            uri = 'faust://inscription/' + space_inscr.group(2) + '/' + space_inscr.group(3) + '/' + space_inscr.group(
                    4)

        wa_para = re.match(r'faust://(inscription|document)/wa/P(.+?)(/(.+?))$', uri)
        if wa_para and wa_para.group(2) in cls.paralipomena:
            sigil = cls.paralipomena[wa_para.group(2)]['sigil']
            para_n = wa_para.group(2)
            inscription = wa_para.group(4) if wa_para.group(4) else ('P' + para_n)
            witness = \
                [witness for witness in cls.database.values() if
                 isinstance(witness, Witness) and witness.sigil == sigil][0]
            result = Inscription(witness, inscription)
            logging.info('Recognized WA paralipomenon: %s -> %s', uri, result)
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

    def __unicode__(self):
        return self.sigil_t

    def __str__(self):
        return codecs.encode(self.__unicode__(), 'ascii', 'xmlcharrefreplace')


def _collect_wits():
    items = defaultdict(list)  # type: Dict[Union[Witness, Inscription, UnknownRef], List[Tuple[str, int]]]
    for macrogenetic_file in faust.macrogenesis_files():
        tree = etree.parse(macrogenetic_file)  # type: etree._ElementTree
        for element in tree.xpath('//f:item', namespaces=faust.namespaces):  # type: etree._Element
            uri = element.get('uri')
            wit = Witness.get(uri, allow_duplicate=True)
            items[wit].append((macrogenetic_file.split('macrogenesis/')[-1], element.sourceline))
    return items


def _assemble_report(wits):
    referenced = [(str(wit), wit.status, ", ".join([file + ":" + str(line) for file, line in wits[wit]]))
                  for wit in sorted(wits, key=str)]

    unused = [(str(wit), "no macrogenesis data", "")
              for wit in sorted(set(Witness.database.values()), key=str)
              if wit not in wits and isinstance(wit, Witness)]

    return [row for row in sorted(referenced, key=lambda r: (r[1], r[0])) if row[1] != '(ok)'] \
           + [row for row in sorted(referenced, key=itemgetter(0)) if row[1] == '(ok)'] \
           + unused


def _report_wits(wits, output_csv='witness-usage.csv'):
    with codecs.open(output_csv, "wt", encoding='utf-8') as reportfile:
        table = csv.writer(reportfile)
        rows = _assemble_report(wits)
        table.writerow(('Zeuge / Referenz', 'Status', 'Vorkommen'))
        for row in rows:
            table.writerow(row)
        stats = Counter([row[1].split(':')[0] for row in rows])
        report = "\n".join('%5d: %s' % (count, status) for status, count in stats.most_common())
        logging.warning('Analyzed references in data:\n%s', report)


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    wits = _collect_wits()

    resolutions = defaultdict(set)
    for uri, result in Witness.get.recorder.keys():
        resolutions[result].add(uri)

    with codecs.open("reference-normalizations.csv", "wt", encoding="utf-8") as resfile:
        table = csv.writer(resfile)
        table.writerow(('in macrogenesis', 'normalisiert'))

        for uri in sorted(resolutions):
            for ref in sorted(resolutions[uri]):
                if str(uri) != str(ref):
                    table.writerow((ref, uri))


    _report_wits(wits)

    wit_sigils = dict()
    for w in [w for w in Witness.database.values() if isinstance(w, Witness)]:
        sigils = dict()
        for uri, sigil in w.other_sigils.items():
            parts = uri.split('/')
            type_, ascii = parts[3:5]
            sigils[type_] = sigil
            sigils[type_ + ' (norm.)'] = ascii
        wit_sigils[w] = sigils

    wit_report = pd.DataFrame(wit_sigils).T
    wit_report.to_excel('witness-sigils.xlsx')

