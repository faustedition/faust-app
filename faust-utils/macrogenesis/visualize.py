import logging
import os.path
import networkx
import faust
import macrogenesis
import pickle

# styles and defaults


DEFAULT_EDGE_WEIGHT = '1'
EDGE_STYLES = [(macrogenesis.KEY_RELATION_NAME, 'temp-pre', 'style', 'solid'),
               (macrogenesis.KEY_RELATION_NAME, 'temp-syn', 'style', 'dashed'),
               (macrogenesis.KEY_RELATION_NAME, macrogenesis.VALUE_IMPLICIT_FROM_ABSOLUTE, 'weight', '5'),
               (macrogenesis.KEY_RELATION_NAME, macrogenesis.VALUE_IMPLICIT_FROM_ABSOLUTE, 'color', 'grey')]

NODE_STYLES = []
STYLE_ABSOLUTE_DATING_CLUSTER_COLOR = 'grey'
STYLE_ABSOLUTE_DATING_COLOR = '#ffffff00'


def apply_agraph_styles(agraph):
    for edge in agraph.edges():
        edge.attr['weight'] = DEFAULT_EDGE_WEIGHT
        for (genetic_key, genetic_value, style_key, style_value) in EDGE_STYLES:
            if genetic_key in edge.attr.keys() and edge.attr[genetic_key] == genetic_value:
                edge.attr[style_key] = style_value

    for node in agraph.nodes():
        for (genetic_key, genetic_value, style_key, style_value) in NODE_STYLES:
            if genetic_key in node.attr.keys() and node.attr[genetic_key] == genetic_value:
                node.attr[style_key] = style_value


def agraph_from(graph):
    # append_absolute_date_nodes(graph)

    logging.info(" Generating agraph.")

    agraph = networkx.nx_agraph.to_agraph(graph)

    # group_absolute_dating_nodes_and_items(agraph)

    # agraph.graph_attr['overlap'] = "1"
    # agraph.graph_attr['concentrate'] = "true"

    visualize_absolute_datings_2(agraph)

    apply_agraph_styles(agraph)

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
                                color=STYLE_ABSOLUTE_DATING_COLOR)
                absolute_dating_nodes.append(date_id)
                absolute_dating_index += 1
            cluster_id = 'cluster{0}'.format(cluster_index)
            agraph.add_subgraph(nbunch=absolute_dating_nodes + [node], name=cluster_id,
                                color=STYLE_ABSOLUTE_DATING_CLUSTER_COLOR)
            cluster_index += 1


def main():
    output_dir = faust.config.get("macrogenesis", "output-dir")

    # draw raw input data
    graph_imported = macrogenesis.import_graph()
    agraph_imported = agraph_from(graph_imported)

    logging.info("Generating raw data graph.")

    agraph_imported.layout(prog='dot')
    agraph_imported.draw(os.path.join(output_dir, '00_raw_data.svg'))
    agraph_imported.write(os.path.join(output_dir, '00_raw_data.dot'))

    # add relationships implicit in absolute datings
    logging.info("Generating graph with implicit absolute date relationships.")

    graph_absolute_edges = graph_imported
    macrogenesis.insert_minimal_edges_from_absolute_datings(graph_imported)
    # del imported_graph

    agraph_absolute_edges = agraph_from(graph_imported)

    agraph_absolute_edges.layout(prog='dot')
    agraph_absolute_edges.draw(os.path.join(output_dir, '10_absolute_edges.svg'))
    agraph_absolute_edges.write(os.path.join(output_dir, '10_absolute_edges.dot'))

    # highlight a single node and its neighbors
    # highlighted_node = 'faust://document/wa/2_I_H.17'
    # highlighted_bunch = imported_graph.neighbors(highlighted_node)
    # highlighted_bunch.append(highlighted_node)
    # highlighted_subgraph = imported_graph.subgraph(nbunch=highlighted_bunch)
    # agraph_highlighted_subgraph = networkx.nx_agraph.to_agraph(highlighted_subgraph)
    # agraph_highlighted_subgraph.layout(prog='dot')
    # agraph_highlighted_subgraph.draw(os.path.join(output_dir, '20_highlighted.svg'))
    # agraph_highlighted_subgraph.write(os.path.join(output_dir, '20_highlighted.dot'))

    # transitive closure, don't draw
    logging.info("Generating transitive closure graph.")
    transitive_closure = networkx.transitive_closure(graph_imported)
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
