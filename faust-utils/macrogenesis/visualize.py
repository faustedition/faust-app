import logging
import os.path

import networkx

import faust
import macrogenesis


def agraph_from(graph):
    logging.info(" Generating agraph.")
    agraph = networkx.nx_agraph.to_agraph(graph)
    #agraph.graph_attr['overlap'] = "1"
    cluster_index = 0

    dated_nodes = []

    for edge in agraph.edges():
        edge.attr['weight'] = '0.5'
#        print edge.attr
#        print edge
        if edge.attr['faust_type'] == 'date':
            logging.debug("Processing date edge {0}".format(edge))

            date = edge[0]
            dated_node = edge[1]
            cluster_id = 'cluster{0}'.format(cluster_index)
            agraph.add_subgraph(nbunch=[date, dated_node], name= cluster_id, color='blue')
            cluster_index += 1
            agraph.remove_edge(edge)
            dated_nodes.append((date, dated_node))


    logging.debug("Sorting dates")
    dated_nodes.sort(key=lambda e: e[0].attr['date_average'])
    previous_dated_node = None
    for edge in dated_nodes:
        if previous_dated_node is not None:
            agraph.add_edge(previous_dated_node, edge[1], color='grey')
        previous_dated_node = edge[1]
    return agraph

def main():
    logging.basicConfig(level=logging.DEBUG)
    output_dir = faust.config.get("macrogenesis", "output-dir")

    # draw raw input data
    imported_graph = macrogenesis.import_graph()
    agraph_imported_graph = agraph_from(imported_graph)

    logging.info("Generating raw data graph.")

    agraph_imported_graph.layout(prog='dot')
    agraph_imported_graph.draw(os.path.join(output_dir, 'raw_data.svg'))
    agraph_imported_graph.write(os.path.join(output_dir, 'raw_data.dot'))

    # highlight a single node and its neighbors
    # highlighted_node = 'faust://document/wa/2_I_H.17'
    # highlighted_bunch = imported_graph.neighbors(highlighted_node)
    # highlighted_bunch.append(highlighted_node)
    # highlighted_subgraph = imported_graph.subgraph(nbunch=highlighted_bunch)
    # agraph_highlighted_subgraph = networkx.nx_agraph.to_agraph(highlighted_subgraph)
    # agraph_highlighted_subgraph.layout(prog='dot')
    # agraph_highlighted_subgraph.draw(os.path.join(output_dir, 'highlighted.svg'))
    # agraph_highlighted_subgraph.write(os.path.join(output_dir, 'highlighted.dot'))

    # transitive closure
    logging.info("Generating transitive closure graph.")
    transitive_closure = networkx.transitive_closure(imported_graph)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))
    agraph_transitive_closure = agraph_from(transitive_closure)

    # draw transitive reduction
    logging.info("Generating transitive reduction graph.")
    agraph_transitive_reduction = agraph_transitive_closure.tred(copy=True)
    logging.info("{0} nodes, {1} edges in transtive reduction.".format(agraph_transitive_reduction.number_of_nodes(),                                                                       agraph_transitive_reduction.number_of_edges()))
    agraph_transitive_reduction.layout(prog='dot')
    agraph_transitive_reduction.draw(os.path.join(output_dir, 'transitive_reduction.svg'))


if __name__ == '__main__':
    main()
