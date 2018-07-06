import json
import logging
import re
import urllib2

from typing import Mapping


class Inscription:

    def __init__(self, witness, inscription):
        self.witness = witness
        self.inscription = inscription

    @property
    def uri(self):
        return "/".join([self.witness.uri.replace('faust://document/', 'faust://inscription/'), self.inscription])

    def __str__(self):
        return self.uri


class UnknownRef:

    def __init__(self, uri):
        self.uri = uri

    def __str__(self):
        return self.uri


class Witness:
    database = {}

    def __init__(self, doc_record):
        if isinstance(doc_record, Mapping):
            self.__dict__.update(doc_record)
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
    def _load_database(cls, url='http://dev.digital-humanities.de/ci/job/faust-gen-fast/lastSuccessfulBuild/artifact/target/uris.json'):
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
            system, sigil, inscription = re.match('^faust://inscription/(.*)/(.*)/(.*)', uri).groups()
            base = "/".join(['faust://document', system, inscription])
            if base in cls.database:
                wit = cls.database[base]
                return Inscription(wit, inscription)

        logging.warning('Unknown reference: %s', uri)
        return UnknownRef(uri)

    def __str__(self):
        return self.uri