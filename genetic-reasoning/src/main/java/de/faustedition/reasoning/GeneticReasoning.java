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
	private Set<Inscription> inscriptions;

	public GeneticReasoning(Set<Inscription> inscriptions) {
		this.inscriptions = inscriptions;
		this.pre = Relations.<Inscription>newTransitiveRelation();
		this.syn = Relations.<Inscription>newTransitiveRelation();
		this.syn = new DefaultRelation<Inscription>();
		//this.para;
		this.tgen = Relations.<Inscription>newTransitiveRelation();
		this.con = new DefaultRelation<Inscription>();//Relations.<Inscription>newTransitiveRelation();
		
	}
	
	
	public void initSyn() {

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.syntagmaticallyPrecedes(i, j))
					syn.relate(i, j);
			}
	}
	
	public void initCon() {
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.exclusivelyContains(i, j))
					con.relate(i, j);
			}
	}

	public void ruleSynImpliesPre() {
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						syn.areRelated(i, j) &&
						! con.areRelated(i, j))					
					con.relate(i, j);
			}
	}
		
	public void ruleConImpliesPre() {	
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						con.areRelated(i, j))					
					pre.relate(i, j);
			}
	}	
}
