package de.faustedition.reasoning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.bath.transitivityutils.ImmutableRelation;

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

					String label = r instanceof PremiseBasedRelation ?
							((PremiseBasedRelation)r).findRelevantPremise(i, j).getName() :
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

	static class ObjectAndScore<E> implements Comparable<ObjectAndScore<E>>{
		public int score = 0;
		public E object;

		@Override
		public int compareTo(ObjectAndScore<E> oas) {
			return this.score - oas.score;
		}
	}


	public static <E> List<E> orderUniverse(ImmutableRelation<E> rel, Set<E> universe) {
		
		List<ObjectAndScore<E>> oasList = new ArrayList<ObjectAndScore<E>>();
		for (E o : universe) {
			ObjectAndScore<E> oas = new ObjectAndScore<E>();
			oas.object = o;
			oasList.add(oas);
		}
		
		for (ObjectAndScore<E> o : oasList) {
			for (ObjectAndScore<E> p : oasList) {
				if (rel.areRelated(o.object, p.object)) {
					o.score--;
					p.score++;
				}
			}
		}
		
		Collections.sort(oasList);
		
		List<E> result = new ArrayList<E>();
		for (ObjectAndScore<E> oas : oasList) {
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
