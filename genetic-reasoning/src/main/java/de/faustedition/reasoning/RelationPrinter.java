package de.faustedition.reasoning;

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
	
	public static void printRelationDot(Relation<Inscription> r, String relationName, String color, Set<Inscription> s, PrintStream stream){

		String edgeattr =  String.format("edge [label=%s color=%s fontcolor=%s]", relationName, color, color);
		stream.println(edgeattr);
		for (Inscription i : s) {
			for (Inscription j : s) {
				if (r.areRelated(i, j)) {
					String nameI = "i_" + i.toString().replaceAll("[ ,:\\(\\)-.]", "_");
					String nameJ = "i_" + j.toString().replaceAll("[ ,:\\(\\)-.]", "_");
					stream.println(" " + nameI + " -> " + nameJ + ";");
				}
			}
			
		}
	}

}
