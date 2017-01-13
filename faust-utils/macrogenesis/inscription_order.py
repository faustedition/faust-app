"""
Calculate a heuristic absolute temporal ordering of all inscriptions
"""

import logging
import os

import networkx

import faust
import graph

def graph_statistics(macrogenetic_graph):
    """Print statistics about the macrogenenesis graph"""

    output_dir = faust.config.get("macrogenesis", "output-dir")
    statistics_file_name = 'statistics.txt'
    with open(os.path.join(output_dir, statistics_file_name), mode='w') as statistics_file:
        statistics_file.write("graph\n")
        statistics_file.write("{0} nodes, {1} edges in macrogenetic graph.\n".format(macrogenetic_graph.number_of_nodes(),
                                                                                     macrogenetic_graph.number_of_edges()))
        absolutely_dated_notes_sorted = graph._absolutely_dated_nodes_sorted(macrogenetic_graph)
        statistics_file.write("{0} nodes with absolute datings.\n".format(len(absolutely_dated_notes_sorted)))
        num_equal_absolute_datings = graph.insert_minimal_edges_from_absolute_datings(macrogenetic_graph)
        statistics_file.write("{0} equal absolute datings.\n".format(num_equal_absolute_datings))

        statistics_file.write("{0} strongly connected components (conflicts).\n".format(
            networkx.number_strongly_connected_components(macrogenetic_graph)))

        transitive_closure = networkx.transitive_closure(macrogenetic_graph)
        statistics_file.write("\ntransitive closure\n")
        statistics_file.write("{0} nodes, {1} edges in transitive closure.\n".format(transitive_closure.number_of_nodes(),
                                                                      transitive_closure.number_of_edges()))
    return [("Graph statistics", statistics_file_name)]


def order_inscriptions(graph):
    """Print a list of inscriptions heuristically ordered by their date of writing"""

    output_dir = faust.config.get("macrogenesis", "output-dir")
    order_file_name = 'order.txt'
    with open(os.path.join(output_dir, order_file_name), mode='w') as order_file:
        transitive_closure = networkx.transitive_closure(graph)
        logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                         transitive_closure.number_of_edges()))
        num_predecessors_nodes = [(len(transitive_closure.predecessors(node)), node) for node in transitive_closure]
        for n in sorted(num_predecessors_nodes, key=lambda tup: tup[0]):
            order_file.write('%i %s\n' % (n[0], n[1]))
            # print '%i %s' % (n[0], n[1])
    return [("Inscription order", order_file_name)]


def analyse_graph():
    logging.basicConfig(level=logging.INFO)
    macrogenetic_graph = graph.import_graph()
    graph.insert_minimal_edges_from_absolute_datings(macrogenetic_graph)

    links = graph_statistics(macrogenetic_graph)
    links = links +  order_inscriptions(macrogenetic_graph)
    return links

if __name__ == '__main__':
    analyse_graph()