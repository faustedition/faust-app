import logging
import os.path
import networkx
import faust
import macrogenesis
import pickle
import base64

# styles and defaults

DEFAULT_EDGE_WEIGHT = '1'
# this causes errors in graphviz in big graphs
# DEFAULT_EDGE_PENWIDTH = '1'
EDGE_STYLES = [(macrogenesis.KEY_RELATION_NAME, 'temp-pre', 'weight', '1'),
               (macrogenesis.KEY_RELATION_NAME, 'temp-pre', 'style', 'solid'),
               (macrogenesis.KEY_RELATION_NAME, 'temp-syn', 'style', 'dashed'),
               (macrogenesis.KEY_RELATION_NAME, 'temp-syn', 'weight', '0.5'),
               (macrogenesis.KEY_RELATION_NAME, 'temp-syn', 'arrowhead', 'none'),

               (macrogenesis.KEY_RELATION_NAME, macrogenesis.VALUE_IMPLICIT_FROM_ABSOLUTE, 'weight', '10'),
               (macrogenesis.KEY_RELATION_NAME, macrogenesis.VALUE_IMPLICIT_FROM_ABSOLUTE, 'color', 'grey')]

NODE_STYLES = []
STYLE_ABSOLUTE_DATING_CLUSTER_COLOR = 'grey'
STYLE_ABSOLUTE_DATING_COLOR = '#ffffff00'


def label_from_uri(uri):
    # label = uri[len('faust://'):]

    if (uri.startswith('faust://document/wa/')):
        return uri[len('faust://document/wa/'):]
    if (uri.startswith('faust://inscription/wa')):
        return uri[len('faust://inscription/wa/'):]
    if (uri.startswith('faust://document/')):
        return uri[len('faust://document/'):]
    if (uri.startswith('faust://inscription/')):
        return uri[len('faust://inscription/'):]
    if (uri.startswith('faust://bibliography/')):
        return uri[len('faust://bibliography/'):]
    if (uri.startswith('faust://')):
        return uri[len('faust://'):]

    #if uri.rindex('/') >= 0:
    #   return uri[uri.rindex('/') + 1:]

    return uri


def apply_agraph_styles(agraph, edge_labels=False):
    for edge in agraph.edges():
        edge.attr['weight'] = DEFAULT_EDGE_WEIGHT
        #edge.attr['penwidth'] = DEFAULT_EDGE_PENWIDTH



        if macrogenesis.KEY_BIBLIOGRAPHIC_SOURCE in edge.attr.keys():
            edge_tooltip = 's. %s &#013;&#013;%s    ->    %s&#013;&#013;Source file: %s' % (
                label_from_uri(edge.attr[macrogenesis.KEY_BIBLIOGRAPHIC_SOURCE]),
                label_from_uri(edge[0]), label_from_uri(edge[1]),
                edge.attr[macrogenesis.KEY_SOURCE_FILE])
            edge.attr['tooltip'] = edge_tooltip
            edge.attr['labeltooltip'] = edge_tooltip

            if edge_labels:
                edge.attr['label'] = label_from_uri(edge.attr[macrogenesis.KEY_BIBLIOGRAPHIC_SOURCE])

        for (genetic_key, genetic_value, style_key, style_value) in EDGE_STYLES:
            if genetic_key in edge.attr.keys() and edge.attr[genetic_key] == genetic_value:
                edge.attr[style_key] = style_value

    for node in agraph.nodes():

        if macrogenesis.KEY_NODE_TYPE in node.attr.keys() and node.attr[macrogenesis.KEY_NODE_TYPE] == macrogenesis.VALUE_ITEM_NODE:
            # link to subgraph for single node neighborhood
            node.attr['URL']='%s.%s' % (highlighted_base_filename(node), 'svg')
            node.attr['label'] = label_from_uri(node)
            node.attr['tooltip'] = '%s &#013;&#013; %s ' \
                                   % (label_from_uri(node), node)

        for (genetic_key, genetic_value, style_key, style_value) in NODE_STYLES:
            if genetic_key in node.attr.keys() and node.attr[genetic_key] == genetic_value:
                node.attr[style_key] = style_value



def agraph_from(graph, edge_labels=False):
    # append_absolute_date_nodes(graph)

    logging.info(" Generating agraph.")

    agraph = networkx.nx_agraph.to_agraph(graph)

    # agraph.graph_attr['overlap'] = "1"
    # agraph.graph_attr['concentrate'] = "true"

    visualize_absolute_datings_2(agraph)

    apply_agraph_styles(agraph, edge_labels)
    agraph.graph_attr['tooltip'] = ' '

    return agraph


def visualize_absolute_datings_2(agraph):
    cluster_index = 0
    absolute_dating_index = 0
    dated_nodes = []

    for node in agraph.nodes():
        if macrogenesis.KEY_ABSOLUTE_DATINGS_PICKLED in node.attr.keys():
            logging.debug("Adding cluster for absolute datings of node {0}".format(node))
            absolute_datings = pickle.loads(node.attr[macrogenesis.KEY_ABSOLUTE_DATINGS_PICKLED])
            absolute_dating_nodes = []
            for absolute_dating in absolute_datings:
                date_id = 'date_{0}'.format(absolute_dating_index)
                date_label = str(absolute_dating)
                agraph.add_node(date_id, label=date_label, date_average=absolute_dating.average, shape='box',
                                color=STYLE_ABSOLUTE_DATING_COLOR, tooltip='s. %s &#013;&#013;Source file: %s'
                                % (label_from_uri(absolute_dating.bibliographic_source), absolute_dating.source_file))
                absolute_dating_nodes.append(date_id)
                absolute_dating_index += 1
            cluster_id = 'cluster{0}'.format(cluster_index)
            agraph.add_subgraph(nbunch=absolute_dating_nodes + [node], name=cluster_id,
                                color=STYLE_ABSOLUTE_DATING_CLUSTER_COLOR)
            cluster_index += 1

def write_html_wrapper(svg_filename, html_filename):
    with open(html_filename, mode='w') as html_file:
        html_file.write('<html><head><script src="macrogenesis_interaction.js" type=></script></head>'
                        '<body><object data="%s" type="image/svg+xml"></object></body>'
                        '</html>'
                        % (svg_filename))
        html_file.close()

def write_agraph_layout (agraph, dir, basename):
    agraph.layout(prog='dot')
    agraph.draw(os.path.join(dir, '%s.%s' % (basename, 'svg')))
    # write_html_wrapper('%s.%s' % (basename, 'svg'), os.path.join(dir, '%s.%s' % (basename, 'html')))
    agraph.write(os.path.join(dir, '%s.%s' % (basename, 'dot')))

def highlighted_base_filename (highlighted_node_url):
    return '20_highlighted_%s' % base64.urlsafe_b64encode(highlighted_node_url)


def main():
    output_dir = faust.config.get("macrogenesis", "output-dir")

    # draw raw input data
    graph_imported = macrogenesis.import_graph()
    agraph_imported = agraph_from(graph_imported)
    logging.info("Generating raw data graph.")
    write_agraph_layout (agraph_imported, output_dir, '00_raw_data')

    # highlight a single node and its neighbors
    # highlighted_node = 'faust://document/wa/2_I_H.17'
    for highlighted_node in graph_imported:
        highlighted_bunch = graph_imported.neighbors(highlighted_node)
        highlighted_bunch.append(highlighted_node)
        graph_highlighted_subgraph = graph_imported.subgraph(nbunch=highlighted_bunch)
        macrogenesis.insert_minimal_edges_from_absolute_datings(graph_highlighted_subgraph)
        agraph_highlighted_subgraph = agraph_from(graph_highlighted_subgraph, edge_labels=True)
        write_agraph_layout(agraph_highlighted_subgraph, output_dir, highlighted_base_filename(highlighted_node))


    # add relationships implicit in absolute datings
    logging.info("Generating graph with implicit absolute date relationships.")
    graph_absolute_edges = graph_imported
    macrogenesis.insert_minimal_edges_from_absolute_datings(graph_absolute_edges)
    del graph_imported
    agraph_absolute_edges = agraph_from(graph_absolute_edges)
    write_agraph_layout(agraph_absolute_edges, output_dir, '10_absolute_edges')
    # again with edge labels
    # TODO this breaks graphviz
    # agraph_absolute_edges_edge_labels = agraph_from(graph_absolute_edges, edge_labels=True)
    # write_agraph_layout(agraph_absolute_edges_edge_labels, output_dir, '15_absolute_edges_edge_labels')

    # transitive closure, don't draw
    logging.info("Generating transitive closure graph.")
    transitive_closure = networkx.transitive_closure(graph_absolute_edges)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))
    agraph_transitive_closure = agraph_from(transitive_closure)

    # draw transitive reduction
    logging.info("Generating transitive reduction graph.")
    agraph_transitive_reduction = agraph_transitive_closure.tred(copy=True)
    logging.info("{0} nodes, {1} edges in transtive reduction.".format(agraph_transitive_reduction.number_of_nodes(),
                                                                       agraph_transitive_reduction.number_of_edges()))
    agraph_transitive_reduction.layout(prog='dot')
    agraph_transitive_reduction.draw(os.path.join(output_dir, '30_transitive_reduction.svg'))


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    main()
