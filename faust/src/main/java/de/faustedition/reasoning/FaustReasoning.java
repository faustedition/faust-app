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
	public Relation<Inscription> econ;
	public Relation<Inscription> pcon;
	public Rules<Inscription> pre;
	private Set<Inscription> inscriptions;
	private Relation<Inscription> synContradictsPre;

	public FaustReasoning(Set<Inscription> inscriptions) {
		this.inscriptions = inscriptions;

		this.syn = Relations.<Inscription>newTransitiveRelation();
		this.econ = new DefaultRelation<Inscription>();//Relations.<Inscription>newTransitiveRelation();
		this.pcon = new DefaultRelation<Inscription>();
		this.pre = new Rules<Inscription>();

	}
	
	public void initSyn() {

		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.syntagmaticallyPrecedesByFirstLine(i, j))
					syn.relate(i, j);
			}
	}
	
	public void initECon() {
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.exclusivelyContains(i, j))
					econ.relate(i, j);
			}
	}

	public void initPCon() {
		
		for (Inscription i: inscriptions)
			for (Inscription j: inscriptions) {
				if (InscriptionRelations.paradigmaticallyContains(i, j))
					pcon.relate(i, j);
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
		initECon();
		initPCon();

		Rule<Inscription> econImpliesPre = new Rule<Inscription>() {
			@Override 
			public String getName() {return "r_econ";}
			@Override
			public boolean premise(Inscription i, Inscription j) {
				return econ.areRelated(i, j);
			}
		};

		Rule<Inscription> pconImpliesPre = new Rule<Inscription>() {
			@Override 
			public String getName() {return "r_pcon";}
			@Override
			public boolean premise(Inscription i, Inscription j) {
				return pcon.areRelated(j, i);
			}
		};

		
		Rule<Inscription> synImpliesPre = new Rule<Inscription>() {
			@Override 
			public String getName() {return "r_syn";}
			@Override
			public boolean premise(Inscription i, Inscription j) {
				return syn.areRelated(i, j);
			}
		};

		pre.add(econImpliesPre);
		pre.add(pconImpliesPre);
		pre.add(synImpliesPre);
	
	}
}
