package de.faustedition.reasoning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	public static List orderUniverse(ImmutableRelation rel, Set universe) {
		
		class ObjectAndScore implements Comparable<ObjectAndScore>{
			public int score = 0;
			public Object object;
			@Override
			public int compareTo(ObjectAndScore oas) {
				return this.score - oas.score;
			}
		}
		
		List<ObjectAndScore> oasList = new ArrayList<ObjectAndScore>();
		for (Object o : universe) {
			ObjectAndScore oas = new ObjectAndScore();
			oas.object = o;
			oasList.add(oas);
		}
		
		for (ObjectAndScore o : oasList) {
			for (ObjectAndScore p : oasList) {
				if (rel.areRelated(o.object, p.object)) {
					o.score--;
					p.score++;
				}
			}
		}
		
		Collections.sort(oasList);
		
		List<Object> result = new ArrayList<Object>();
		for (ObjectAndScore oas : oasList) {
			result.add(oas.object);
		}
		return result;
	}
	
	public static void printInscriptionCSV (List<Inscription> inscriptions,
			int fromLine, int toLine, String filename) throws FileNotFoundException {

		FileOutputStream out;
		PrintStream ps;
		out = new FileOutputStream(filename);
		ps = new PrintStream(out);

		try {
			for (int line = fromLine; line <= toLine; line++) {
				ps.print(line + ",");
				for (Inscription i : inscriptions) {
					if (i.contains(line))
						ps.print(i.toString().replaceAll("[ ,:\\(\\)-.]", "_") + ",");
					else
						ps.print(" ,");
				}
				ps.println();
			}
		} finally {
			ps.close();
		}
	}
	

}
