package de.faustedition.reasoning;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.faustedition.reasoning.PremiseBasedRelation.Premise;

import edu.bath.transitivityutils.Relation;

public class LastPremiseRelationTest {

	private Set<String> universe;
	
	private Relation<String> relA;
	private Relation<String> relB;
	private Relation<String> relC;
	private Relation<String> relD;

	private Premise<String> preA;
	private Premise<String> preB;
	private Premise<String> preC;

	
	@Before
	public void init() {
		universe = new HashSet<String>();
		universe.add("A");
		universe.add("B");
		universe.add("C");
		universe.add("D");
			
		relA = MultimapBasedRelation.create();
		relA.relate("A", "B");
		relA.relate("B", "C");
		relA.relate("D", "C");

		relB = MultimapBasedRelation.create();
		relB.relate("A", "B");

		relC = MultimapBasedRelation.create();
		relC.relate("C", "D");
		
		preA = new Premise<String>() {
			@Override
			public String getName() {
				return "A";
			}
			@Override
			public boolean applies(String o, String p) {
				return relA.areRelated(o, p);
			}
		};
		preB = new Premise<String>() {
			@Override
			public String getName() {
				return "B";
			}
			@Override
			public boolean applies(String o, String p) {
				return relB.areRelated(o, p);
			}
		};
		preC = new Premise<String>() {
			@Override
			public String getName() {
				return "C";
			}
			@Override
			public boolean applies(String o, String p) {
				return relC.areRelated(o, p);
			}
		};


	}
	
	
	
	@Test
	public void testAreRelated() {
		LastPremiseRelation<String> lpr = new LastPremiseRelation<String>(preC, preB, preA);
		assertFalse(lpr.areRelated("A", "B"));
		assertFalse(lpr.areRelated("B", "A"));
		assertTrue(lpr.areRelated("B", "C"));
		assertFalse(lpr.areRelated("C", "B"));
		assertFalse(lpr.areRelated("C", "D"));
		assertFalse(lpr.areRelated("D", "C"));
		
	}
}
