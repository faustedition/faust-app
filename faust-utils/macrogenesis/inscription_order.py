import logging

import networkx

import macrogenesis


def main():
    logging.basicConfig(level=logging.INFO)
    imported_graph = macrogenesis.import_graph()
    macrogenesis.insert_minimal_edges_from_absolute_datings(imported_graph)
    transitive_closure = networkx.transitive_closure(imported_graph)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))

    num_predecessors_nodes = [(len(transitive_closure.predecessors(node)), node) for node in transitive_closure]
    for n in sorted(num_predecessors_nodes, key=lambda tup: tup[0]):
        print '%i %s' % (n[0], n[1])
        # print n


if __name__ == '__main__':
    main()
