package de.faustedition.reasoning;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;

import edu.bath.transitivityutils.Relation;
import edu.bath.transitivityutils.Relations;
import edu.bath.transitivityutils.TransitiveRelation;
public class GeneticReasoning {

	public Relation<Inscription> pre;
	public Relation<Inscription> syn;
	//public Relation<Inscription> para;
	public Relation<Inscription> tgen;
	public Relation<Inscription> con;

	public GeneticReasoning() {
		this.pre = Relations.<Inscription>newTransitiveRelation();
		this.syn = Relations.<Inscription>newTransitiveRelation();
		//this.para;
		this.tgen = Relations.<Inscription>newTransitiveRelation();
		this.con = Relations.<Inscription>newTransitiveRelation();
	}
	
	private static void printRelation(Relation<Inscription> r, String relationName, Set<Inscription> s, PrintStream stream){
		stream.println("digraph " + relationName + " {");
		stream.println("edge [label=" + relationName + "];");
		for (Inscription i : s) {
			for (Inscription j : s) {
				if (r.areRelated(i, j)) {
					String nameI = "i_" + i.toString().replaceAll("[ ,:\\(\\)-]", "_");
					String nameJ = "i_" + j.toString().replaceAll("[ ,:\\(\\)-]", "_");
					stream.println(" " + nameI + " -> " + nameJ + ";");
				}
			}
			
		}
		stream.println("}");
	}
	
	public void reason (Set<Inscription> inscriptions) {

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.syntagmaticallyPrecedes(i, j))
					syn.relate(i, j);
			}
		
		printRelation(syn, "syn", inscriptions, System.out);
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.exclusivelyContains(i, j))
					con.relate(i, j);
			}

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						syn.areRelated(i, j) &&
						! con.areRelated(i, j))					
					con.relate(i, j);
			}
		

		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						con.areRelated(i, j))					
					pre.relate(i, j);
			}

		printRelation(pre, "pre", inscriptions, System.out);

		
	}
	
}
