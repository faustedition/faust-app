import logging

import networkx

import macrogenesis

def analyse_graph(graph):
    """Print statistics about the macrogenenesis graph"""
    print "graph"
    print "{0} nodes, {1} edges in macrogenetic graph.".format(graph.number_of_nodes(),
                                                                     graph.number_of_edges())
    absolutely_dated_notes_sorted = macrogenesis.absolutely_dated_nodes_sorted(graph)
    print "{0} nodes with absolute datings.".format(len(absolutely_dated_notes_sorted))
    num_equal_absolute_datings = macrogenesis.insert_minimal_edges_from_absolute_datings(graph)
    print "{0} equal absolute datings.".format(num_equal_absolute_datings)

    print "{0} strongly connected components (conflicts).".format(networkx.number_strongly_connected_components(graph))

    transitive_closure = networkx.transitive_closure(graph)
    print "transitive closure"
    print "{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges())



def order_inscriptions(graph):
    """Print a list of inscriptions heuristically ordered by their date of writing"""

    transitive_closure = networkx.transitive_closure(graph)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))
    num_predecessors_nodes = [(len(transitive_closure.predecessors(node)), node) for node in transitive_closure]
    for n in sorted(num_predecessors_nodes, key=lambda tup: tup[0]):
        print '%i %s' % (n[0], n[1])



if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    graph = macrogenesis.import_graph()
    macrogenesis.insert_minimal_edges_from_absolute_datings(graph)

    analyse_graph(graph)
    #order_inscriptions(graph)