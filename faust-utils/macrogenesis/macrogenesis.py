import networkx
import lxml.etree as etree
import logging
import faust


def label_from_uri(uri):
    # label = uri[len('faust://'):]
    label = uri[uri.rindex('/') + 1:]
    return label


def parse(macrogenetic_file, graph):
    logging.info("Parsing file {0}".format(str(macrogenetic_file)))
    macrogenetic_document = etree.parse(macrogenetic_file)
    relations = macrogenetic_document.getroot().findall('f:relation', namespaces=faust.namespaces)
    for relation in relations:
        try:
            relation_name = relation.attrib["name"]
            if relation_name != 'temp-pre':
                break

            info_message = ' ' + str(relation_name) + ': '
            source_uri = relation.find('f:source', namespaces=faust.namespaces).attrib['uri']
            items = relation.findall('f:item', namespaces=faust.namespaces)
            previous_item = None
            previous_item_uri = None
            for item in items:
                item_uri = item.attrib["uri"]
                graph.add_node(item_uri, label=label_from_uri(item_uri))
                if previous_item is None:
                    info_message += '   '
                else:
                    graph.add_edge(previous_item_uri, item_uri,  # label=label_from_uri(source_uri),
                                   edgetooltip=str(macrogenetic_file))
                    info_message += ' > '
                info_message = info_message + ' ' + str(item_uri)
                previous_item = item
                previous_item_uri = item_uri
            logging.info(info_message)
        except Exception as e:
            logging.error("Error in file " + str(macrogenetic_file))
            logging.exception(e)

            # dates =  macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)


def import_graph():
    imported_graph = networkx.MultiDiGraph()
    imported_graph = networkx.MultiDiGraph()
    for macrogenetic_file in faust.macrogenesis_files():
        parse(macrogenetic_file, imported_graph)

    logging.info(
        "{0} nodes, {1} edges read.".format(imported_graph.number_of_nodes(), imported_graph.number_of_edges()))
    return imported_graph


def main():
    logging.basicConfig(level=logging.INFO)
    import_graph()


if __name__ == '__main__':
    main()
