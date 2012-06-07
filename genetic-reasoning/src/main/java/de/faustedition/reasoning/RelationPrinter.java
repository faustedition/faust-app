package de.faustedition.reasoning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import edu.bath.transitivityutils.Relation;

public class RelationPrinter {

	public static void startDot(String graphName, PrintStream stream) {
		stream.println("digraph " + graphName + "genetic_graph {");
	}

	public static void endDot(PrintStream stream) {
		stream.println("}");
	}

	public static void printRelationDot(ImmutableRelation r,
			String relationName, String color, int weight, Set s,
			PrintStream stream) {

		for (Object i : s) {
			for (Object j : s) {
				if (r.areRelated(i, j)) {

					String label = r instanceof Rules ?
							((Rules)r).relevantRule(i, j).getName() :
								relationName;
					
					String edgeattr = String.format(
							"edge [label=%s color=%s fontcolor=%s weight=%d]",
							label, color, color, weight);
					stream.println(edgeattr);

					String nameI = "i_"
							+ i.toString().replaceAll("[ ,:\\(\\)-.]", "_");
					String nameJ = "i_"
							+ j.toString().replaceAll("[ ,:\\(\\)-.]", "_");
					stream.println(" " + nameI + " -> " + nameJ + ";");
				}
			}
		}
	}

	public static void printGraph(ImmutableRelation relation, String name,
			String color, int weight, Set<Inscription> inscriptions,
			String filename) throws FileNotFoundException {

		FileOutputStream out;
		PrintStream ps;
		out = new FileOutputStream(filename);
		ps = new PrintStream(out);

		try {

			RelationPrinter.startDot(name, ps);
			RelationPrinter.printRelationDot(relation, name, color, weight,
					inscriptions, ps);
			RelationPrinter.endDot(ps);
		} finally {
			ps.close();
		}
	}

}
