package de.faustedition.reasoning;
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
	
	public void reason (Set<Inscription> inscriptions) {

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.syntagmaticallyPrecedes(i, j))
					this.syn.relate(i, j);
			}
		
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.exclusivelyContains(i, j))
					this.con.relate(i, j);
			}

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						this.syn.areRelated(i, j) &&
						! this.con.areRelated(i, j))					
					this.con.relate(i, j);
			}
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						this.con.areRelated(i, j))					
					this.con.relate(i, j);
			}
		
	}
	
	
	public static void main(String[] args) {
		
		Relation<Integer> prec = Relations.newTransitiveRelation();
		
		final int size = 500;
		final Random rand = new Random();
		for (int i=0; i < size; i++) {
			for (int j=0; j < size; j++) {
				if (rand.nextInt(500) == 1)				
					prec.relate(i, j);
			}			
		}

		for (int i=0; i < size; i++) {
			for (int j=0; j < size; j++) {
				if (prec.areRelated(i, j))
				System.out.println(" " + i + " -> " + j);
			}			
		}

	}
}
