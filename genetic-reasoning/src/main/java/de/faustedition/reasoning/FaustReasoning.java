package de.faustedition.reasoning;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;

import edu.bath.transitivityutils.Relation;
import edu.bath.transitivityutils.Relations;
import edu.bath.transitivityutils.TransitiveRelation;

/**
 * Faust-specific relations and rules.
 *
 */
public class FaustReasoning {


	public Relation<Inscription> syn;
	public Relation<Inscription> con;
	public Rules<Inscription> pre;
	private Set<Inscription> inscriptions;
	private Relation<Inscription> synContradictsPre;

	public FaustReasoning(Set<Inscription> inscriptions) {
		this.inscriptions = inscriptions;

		this.syn = Relations.<Inscription>newTransitiveRelation();
		this.con = Relations.<Inscription>newTransitiveRelation();
		this.pre = new Rules<Inscription>();

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
	
			
	/**
	 * @param relA 
	 * @param relB is supposed to be the overriding or "stronger" relation
	 * @return  A relation result where result(i, j) if relA(i,j) && relB(j,i)
	 */
	public Relation<Inscription> contradictions(ImmutableRelation<Inscription> relA, ImmutableRelation<Inscription> relB) {
		Relation<Inscription> result = new DefaultRelation<Inscription>();
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (
						relA.areRelated(i, j) &&
						relB.areRelated(j, i))					
					result.relate(i, j);
			}		
		return result;
	}
	
	
	public void reason() {
		
		initSyn();
		initCon();

		Rule<Inscription> conImpliesPre = new Rule<Inscription>() {
			@Override
			public boolean premise(Inscription i, Inscription j) {
				return con.areRelated(i, j);
			}
		};

		Rule<Inscription> synImpliesPre = new Rule<Inscription>() {
			@Override
			public boolean premise(Inscription i, Inscription j) {
				return syn.areRelated(i, j);
			}
		};

		pre.add(conImpliesPre);
		pre.add(synImpliesPre);
	
	}
}
