import logging
import os.path

import networkx

import faust
import macrogenesis


def main():
    logging.basicConfig(level=logging.INFO)
    output_dir = faust.config.get("macrogenesis", "output-dir")

    # draw raw input data
    imported_graph = macrogenesis.import_graph()
    agraph_imported_graph = networkx.nx_agraph.to_agraph(imported_graph)
    agraph_imported_graph.layout(prog='dot')
    agraph_imported_graph.draw(os.path.join(output_dir, 'raw_data.svg'))
    agraph_imported_graph.write(os.path.join(output_dir, 'raw_data.dot'))

    # highlight a single node and its neighbors
    highlighted_node = 'faust://document/wa/2_I_H.17'
    highlighted_bunch = imported_graph.neighbors(highlighted_node)
    highlighted_bunch.append(highlighted_node)
    highlighted_subgraph = imported_graph.subgraph(nbunch=highlighted_bunch)
    agraph_highlighted_subgraph = networkx.nx_agraph.to_agraph(highlighted_subgraph)
    agraph_highlighted_subgraph.layout(prog='dot')
    agraph_highlighted_subgraph.draw(os.path.join(output_dir, 'highlighted.svg'))
    agraph_highlighted_subgraph.write(os.path.join(output_dir, 'highlighted.dot'))

    # transitive closure
    transitive_closure = networkx.transitive_closure(imported_graph)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))
    agraph_transitive_closure = networkx.nx_agraph.to_agraph(transitive_closure)

    # draw transitive reduction
    agraph_transitive_reduction = agraph_transitive_closure.tred(copy=True)
    logging.info("{0} nodes, {1} edges in transtive reduction.".format(agraph_transitive_reduction.number_of_nodes(),
                                                                       agraph_transitive_reduction.number_of_edges()))
    agraph_transitive_reduction.layout(prog='dot')
    agraph_transitive_reduction.draw(os.path.join(output_dir, 'transitive_reduction.svg'))


if __name__ == '__main__':
    main()
