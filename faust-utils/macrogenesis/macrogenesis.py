import networkx
import lxml.etree as etree
import logging
import faust
import datetime


def label_from_uri(uri):
    # label = uri[len('faust://'):]
    label = uri[uri.rindex('/') + 1:]
    return label


def parse(macrogenetic_file, graph):
    logging.info("Parsing file {0}".format(str(macrogenetic_file)))
    macrogenetic_document = etree.parse(macrogenetic_file)
    parse_relationships(macrogenetic_document, graph)
    parse_dates(macrogenetic_document, graph)


def parse_datestr(datestr):
    return None if datestr is None else datetime.datetime.strptime(datestr, '%Y-%m-%d')


# datetime.strftime cannot handle years before 1900, so we have to roll our own
def format_date(date):
    if date is None:
        return '...'
    else:
        return '{0}.{1}.{2}'.format(date.day, date.month, date.year)


def parse_dates(macrogenetic_document, graph):
    dates = macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)

    for (date_index, date) in enumerate(dates):
        date_when = parse_datestr(date.attrib["when"]) if date.attrib.has_key("when") else None

        # simplify and treat 'notBefore' and 'notAfter' like 'from' and 'to'

        date_from = parse_datestr(
            date.attrib["from"] if date.attrib.has_key("from") else date.attrib["notBefore"] if date.attrib.has_key(
                "notBefore") else None)
        date_to = parse_datestr(
            date.attrib["to"] if date.attrib.has_key("to") else date.attrib["notAfter"] if date.attrib.has_key(
                "notAfter") else None)

        if date_when is not None:
            date_average = date_when
        else:
            if date_from is not None and date_to is None:
                date_average = date_from
            else:
                if date_to is not None and date_from is None:
                    date_average = date_to
                else:
                    if date_from is not None and date_to is not None:
                        date_average = date_from + ((date_to - date_from) / 2)
                    else:
                        break

        source_uri = date.find('f:source', namespaces=faust.namespaces).attrib['uri']

        date_id = 'date_{0}'.format(date_index)
        date_label = "{0}".format(format_date(date_when)) if date_when is not None else "{0} - {1}" \
            .format(format_date(date_from), format_date(date_to), format_date(date_average))
        logging.info(date_label)

        items = date.findall('f:item', namespaces=faust.namespaces)
        for item in items:
            item_uri = item.attrib["uri"]
            graph.add_node(date_id, label=date_label, date_average=date_average, shape='box', color='#ffffff00')
            graph.add_edge(date_id, item_uri, color='grey', faust_type='date')


def parse_relationships(macrogenetic_document, graph):
    relations = macrogenetic_document.getroot().findall('f:relation', namespaces=faust.namespaces)
    for relation in relations:
        try:
            relation_name = relation.attrib["name"]
            edge_style = 'solid' if relation_name == 'temp-pre' else 'dashed'
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
                                   # edgetooltip=str(macrogenetic_file),
                                   style=edge_style)
                    info_message += ' > '
                info_message = info_message + ' ' + str(item_uri)
                previous_item = item
                previous_item_uri = item_uri
            logging.info(info_message)
        except Exception as e:
            logging.error("Error parsing relation " + str(relation_name))
            logging.exception(e)

            # dates =  macrogenetic_document.getroot().findall('f:date', namespaces=faust.namespaces)


def import_graph():
    imported_graph = networkx.MultiDiGraph()
    imported_graph = networkx.MultiDiGraph()
    for macrogenetic_file in faust.macrogenesis_files()[2:5]:
        parse(macrogenetic_file, imported_graph)

    logging.info(
        "{0} nodes, {1} edges read.".format(imported_graph.number_of_nodes(), imported_graph.number_of_edges()))
    return imported_graph


def main():
    logging.basicConfig(level=logging.INFO)
    import_graph()


if __name__ == '__main__':
    main()
