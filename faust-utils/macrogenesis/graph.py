"""
Parse macrogenetic XML files into a networkx graph
"""
import json
import pickle
import re
import sys
import urllib2
from collections import Mapping

import networkx
import lxml.etree as etree
import logging
import faust
import datetime
import base64

KEY_RELATION_NAME = 'relation_name'
KEY_ABSOLUTE_DATINGS = 'absolute_datings'
KEY_ABSOLUTE_DATINGS_PICKLED = 'absolute_datings_pickled'
KEY_BIBLIOGRAPHIC_SOURCE = 'bibliographic_source'
KEY_SOURCE_FILE = 'source_file'

KEY_NODE_TYPE = 'node_type'

VALUE_IMPLICIT_FROM_ABSOLUTE = 'implicit_from_absolute'

VALUE_ITEM_NODE = 'item_node'

GENETIC_RELATION_MAP = {'temp-pre': 'temp-pre',
                        'temp-syn': 'temp-syn'}


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


class AbsoluteDating:
    """Represent an absolute dating"""

    # None can be passed for missing values
    def __init__(self, when, from_, to, not_before, not_after, bibliographic_source=None, source_file=None):
        self.when = when
        self.from_ = from_
        self.to = to
        self.not_before = not_before
        self.not_after = not_after
        self.bibliographic_source = bibliographic_source
        self.source_file = source_file

        if self.when is not None:
            date_average = self.when
        else:
            if self.first_possible() is not None and self.last_possible() is None:
                # for an open interval, add a second so it is > compared to pure @when date
                date_average = self.first_possible() + datetime.timedelta(seconds=1)
            else:
                if self.last_possible() is not None and self.first_possible() is None:
                    # for an open interval, subtract a second so it is < compared to pure @when date
                    date_average = self.last_possible() - datetime.timedelta(seconds=1)
                else:
                    if self.first_possible() is not None and self.last_possible() is not None:
                        date_average = self.first_possible() + ((self.last_possible() - self.first_possible()) / 2)
                    else:
                        raise ValueError('Wrong combination of arguments for AbsoluteDating, or too few.')

        self.average = date_average

    def __str__(self):
        return "{0}".format(_format_date(self.when)) if self.when is not None else "{0} - {1}" \
            .format(_format_date(self.first_possible()), _format_date(self.last_possible()), _format_date(self.average))

    # Returns not_before or, if not available, from date
    def first_possible(self):
        if (self.not_before is not None):
            return self.not_before
        else:
            return self.from_

    # Returns not_after or, if not available, to date
    def last_possible(self):
        if (self.not_after is not None):
            return self.not_after
        else:
            return self.to


# datetime.strftime cannot handle years before 1900, so we have to roll our own
def _format_date(date):
    """Return a string representing a datetime object"""
    if date is None:
        return '...'
    else:
        return '{0}.{1}.{2}'.format(date.day, date.month, date.year)


def _parse(macrogenetic_file, graph):
    """Parse one XML file and augment graph by the extracted information."""
    logging.info("Parsing file {0}".format(str(macrogenetic_file)))
    macrogenetic_document = etree.parse(macrogenetic_file)
    _parse_relationships(macrogenetic_document, graph)
    _parse_dates(macrogenetic_document, graph)


def _parse_datestr(datestr):
    """Parse a datestring and return a datetime object"""
    return None if datestr is None else datetime.datetime.strptime(datestr, '%Y-%m-%d')


def serialize_for_graphviz(obj):
    """Serialize a python object to be stored in graphviz attributes"""
    return base64.b64encode(pickle.dumps(obj))


def deserialize_from_graphviz(serialized):
    """Deserialize a python object transported in graphviz attributes"""
    return pickle.loads(base64.b64decode(serialized))


def _average_absolute_date(node_attr):
    """Read and return the stored average date from a node"""
    # TODO average of all dates, not just first one
    return node_attr[KEY_ABSOLUTE_DATINGS][0].average


def insert_minimal_edges_from_absolute_datings(graph):
    """
    Append a networkx graph with information that is in the absolute datings.
    This is done by ordering the nodes by absoulte dating and inserting edges between adjacent entries in the list.
    This provides a minimal set of edges spanning the whole graph.
    """
    absolutely_dated_nodes = _absolutely_dated_nodes_sorted(graph)
    same_dates_count = 0
    previous_dated_node_id = None
    last_differently_dated_node_id = None
    previous_date = None
    for node in absolutely_dated_nodes:
        if previous_dated_node_id is not None:
            if previous_date == _average_absolute_date(node[1]):
                same_dates_count += 1
            else:
                last_differently_dated_node_id = previous_dated_node_id
            if last_differently_dated_node_id is not None:
                graph.add_edge(last_differently_dated_node_id, node[0],
                               attr_dict={KEY_RELATION_NAME: VALUE_IMPLICIT_FROM_ABSOLUTE})
        previous_dated_node_id = node[0]
        previous_date = _average_absolute_date(node[1])
    return same_dates_count


def _absolutely_dated_nodes_sorted(graph):
    """Sort nodes according to their absolute datings"""
    # list of (node_id, node_attr) tuples
    absolutely_dated_nodes = [(n, graph.node[n]) for n in graph.nodes() if KEY_ABSOLUTE_DATINGS in graph.node[n].keys()]
    logging.debug("Sorting dates")
    absolutely_dated_nodes.sort(key=lambda n: _average_absolute_date(n[1]))
    return absolutely_dated_nodes


# call parse_relationships() first to initialize graph
def _parse_dates(macrogenetic_document, graph):
    """Parse one XML document for absolute datings and augment graph with the extracted data"""
    dates = macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)

    for (date_index, date) in enumerate(dates):

        try:

            source_uri = date.find('f:source', namespaces=faust.namespaces).attrib['uri']

            absolute_dating = AbsoluteDating(
                    _parse_datestr(date.attrib["when"] if date.attrib.has_key("when") else None),
                    _parse_datestr(date.attrib["from"] if date.attrib.has_key("from") else None),
                    _parse_datestr(date.attrib["to"] if date.attrib.has_key("to") else None),
                    _parse_datestr(date.attrib["notBefore"] if date.attrib.has_key("notBefore") else None),
                    _parse_datestr(date.attrib["notAfter"] if date.attrib.has_key("notAfter") else None),
                    bibliographic_source=source_uri, source_file=macrogenetic_document.docinfo.URL)

            date_id = 'date_{0}'.format(date_index)
            date_label = str(absolute_dating)
            logging.info('Parsed Date: %s' % (date_label))

            items = date.findall('f:item', namespaces=faust.namespaces)
            absolute_datings = []
            for item in items:
                # TODO normalize uris
                item_uri = item.attrib["uri"]
                if not item_uri in graph.node:
                    _add_item_node(graph, item_uri)

                absolute_datings.append(absolute_dating)

            if len(absolute_datings) > 0:
                graph.node[item_uri][KEY_ABSOLUTE_DATINGS] = absolute_datings
                # Also pickle this because when converting to an AGraph attribute values must be strings
                graph.node[item_uri][KEY_ABSOLUTE_DATINGS_PICKLED] = serialize_for_graphviz(absolute_datings)

        except Exception as e:
            logging.error("Invalid absolute dating in %s" % (macrogenetic_document.docinfo.URL))


def _parse_relationships(macrogenetic_document, graph):
    """Parse one XML document for relative datings and augment graph with the extracted data"""
    relations = macrogenetic_document.getroot().findall('f:relation', namespaces=faust.namespaces)
    for relation in relations:
        try:
            relation_name = relation.attrib["name"]
            info_message = ' ' + str(relation_name) + ': '
            source_uri = relation.find('f:source', namespaces=faust.namespaces).attrib['uri']
            items = relation.findall('f:item', namespaces=faust.namespaces)
            previous_item = None
            previous_item_uri = None
            for item in items:
                item_uri = Witness.get(item.attrib["uri"]).uri
                # TODO create label in visualization component
                _add_item_node(graph, item_uri)
                if previous_item is None:
                    info_message += '   '
                else:
                    edge_attr_dict = {}
                    if relation_name in GENETIC_RELATION_MAP.keys():
                        edge_attr_dict[KEY_RELATION_NAME] = GENETIC_RELATION_MAP[relation_name]
                    else:
                        raise ValueError("Unknown relation {0} encountered".format(relation_name))

                    edge_attr_dict[KEY_BIBLIOGRAPHIC_SOURCE] = source_uri
                    edge_attr_dict[KEY_SOURCE_FILE] = macrogenetic_document.docinfo.URL

                    # eliminate double edges (edges with same bibliographic source) TODO merge all information of both
                    edges_from_previous_to_this = [edge for edge in graph.out_edges([previous_item_uri], data=True) if
                                                   edge[1] == item_uri]
                    used_bibliographic_sources = [edge[2][KEY_BIBLIOGRAPHIC_SOURCE] for edge in
                                                  edges_from_previous_to_this if
                                                  edge[2].has_key(KEY_BIBLIOGRAPHIC_SOURCE)]
                    if not source_uri in used_bibliographic_sources:
                        # if not relation_name == 'temp-syn':
                        graph.add_edge(previous_item_uri, item_uri, attr_dict=edge_attr_dict)
                        # make edges two directional for synchronous relation?
                        # if relation_name == 'temp-syn':
                        #    graph.add_edge(item_uri, previous_item_uri,  # label=label_from_uri(source_uri),
                        #     # edgetooltip=str(macrogenetic_file),
                        #     attr_dict=edge_attr_dict)

                    info_message += ' > '
                info_message = info_message + ' ' + str(item_uri)
                previous_item = item
                previous_item_uri = item_uri
            logging.info(info_message)
        except Exception as e:
            logging.error("Error parsing relation " + str(relation_name))
            logging.exception(e)

            # dates =  macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)


def _add_item_node(graph, item_uri):
    wit = Witness.get(item_uri)
    attrs = {KEY_NODE_TYPE: VALUE_ITEM_NODE}
    if hasattr(wit, 'sigil'):
        attrs['label'] = wit.sigil
    graph.add_node(wit.uri, attr_dict=attrs)


def import_graph():
    imported_graph = networkx.MultiDiGraph()
    for macrogenetic_file in faust.macrogenesis_files():  # [10:13]:#[3:6]:
        _parse(macrogenetic_file, imported_graph)

    logging.info(
            "{0} nodes, {1} edges read.".format(imported_graph.number_of_nodes(), imported_graph.number_of_edges()))
    return imported_graph


def main():
    import_graph()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    main()
