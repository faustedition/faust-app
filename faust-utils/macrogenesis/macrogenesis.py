import pickle
import networkx
import lxml.etree as etree
import logging
import faust
import datetime


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


class AbsoluteDating:
    # None can be passed for missing values
    def __init__(self, when, from_, to, not_before, not_after, bibliographic_source=None, source_file=None):
        self.when = when
        self.from_ = from_
        self.to = to
        self.not_before = not_before
        self.not_after = not_after
        self.bibliographic_source=bibliographic_source
        self.source_file=source_file

        if self.when is not None:
            date_average = self.when
        else:
            if self.first_possible() is not None and self.last_possible() is None:
                date_average = self.first_possible()
            else:
                if self.last_possible() is not None and self.first_possible() is None:
                    date_average = self.last_possible()
                else:
                    if self.first_possible() is not None and self.last_possible() is not None:
                        date_average = self.first_possible() + ((self.last_possible() - self.first_possible()) / 2)
                    else:
                        raise ValueError('Wrong combination of arguments for AbsoluteDating, or too few.')

        self.average = date_average

    def __str__(self):
        return "{0}".format(format_date(self.when)) if self.when is not None else "{0} - {1}" \
            .format(format_date(self.first_possible()), format_date(self.last_possible()), format_date(self.average))

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
def format_date(date):
    if date is None:
        return '...'
    else:
        return '{0}.{1}.{2}'.format(date.day, date.month, date.year)




def parse(macrogenetic_file, graph):
    logging.info("Parsing file {0}".format(str(macrogenetic_file)))
    macrogenetic_document = etree.parse(macrogenetic_file)
    parse_relationships(macrogenetic_document, graph)
    parse_dates(macrogenetic_document, graph)


def parse_datestr(datestr):
    return None if datestr is None else datetime.datetime.strptime(datestr, '%Y-%m-%d')


def insert_minimal_edges_from_absolute_datings(graph):
    # list of (node_id, node_attr) tuples
    absolutely_dated_nodes = [(n, graph.node[n]) for n in graph.nodes() if KEY_ABSOLUTE_DATINGS in graph.node[n].keys()]
    logging.debug("Sorting dates")
    absolutely_dated_nodes.sort(key=lambda n: n[1][KEY_ABSOLUTE_DATINGS][0].average)
    previous_dated_node = None
    for node in absolutely_dated_nodes:
        if previous_dated_node is not None:
            graph.add_edge(previous_dated_node, node[0], attr_dict={KEY_RELATION_NAME: VALUE_IMPLICIT_FROM_ABSOLUTE})
        previous_dated_node = node[0]


# call parse_relationships() first to initialize graph
def parse_dates(macrogenetic_document, graph):
    dates = macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)

    for (date_index, date) in enumerate(dates):

        try:

            source_uri = date.find('f:source', namespaces=faust.namespaces).attrib['uri']

            absolute_dating = AbsoluteDating(
                parse_datestr(date.attrib["when"] if date.attrib.has_key("when") else None),
                parse_datestr(date.attrib["from"] if date.attrib.has_key("from") else None),
                parse_datestr(date.attrib["to"] if date.attrib.has_key("to") else None),
                parse_datestr(date.attrib["notBefore"] if date.attrib.has_key("notBefore") else None),
                parse_datestr(date.attrib["notAfter"] if date.attrib.has_key("notAfter") else None),
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
                    add_item_node(graph, item_uri)

                absolute_datings.append(absolute_dating)

            if len(absolute_datings) > 0:
                graph.node[item_uri][KEY_ABSOLUTE_DATINGS] = absolute_datings
                # Also pickle this because when converting to an AGraph attribute values must be strings
                graph.node[item_uri][KEY_ABSOLUTE_DATINGS_PICKLED] = pickle.dumps(absolute_datings)

        except Exception as e:
            logging.error("Invalid absolute dating in %s" % (macrogenetic_document.docinfo.URL))


def parse_relationships(macrogenetic_document, graph):
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
                item_uri = item.attrib["uri"]
                # TODO create label in visualization component
                add_item_node(graph, item_uri)
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

                    graph.add_edge(previous_item_uri, item_uri,
                                   attr_dict=edge_attr_dict)
                    # make edges two directional for synchronous relation?
                    # if relation_name == 'temp-syn':
                    #     graph.add_edge(item_uri, previous_item_uri,  # label=label_from_uri(source_uri),
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


def add_item_node(graph, item_uri):
    graph.add_node(item_uri, attr_dict={KEY_NODE_TYPE: VALUE_ITEM_NODE})


def import_graph():
    imported_graph = networkx.MultiDiGraph()
    # FIXME parse whole set of files
    for macrogenetic_file in faust.macrogenesis_files(): #[10:13]:#[3:6]:
        parse(macrogenetic_file, imported_graph)

    logging.info(
        "{0} nodes, {1} edges read.".format(imported_graph.number_of_nodes(), imported_graph.number_of_edges()))
    return imported_graph


def main():
    import_graph()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    main()
