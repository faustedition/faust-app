package de.faustedition.reasoning;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.bath.transitivityutils.Relation;

public class StatisticsTest {

	private Set<String> universe;
	
	private Relation<String> relA;
	private Relation<String> relB;
	private Relation<String> relC;
	private Relation<String> relD;
	
	private float delta = 0.01f;
	
	@Before
	public void init() {
		universe = new HashSet<String>();
		universe.add("A");
		universe.add("B");
		universe.add("C");
		
		relA = MultimapBasedRelation.create();
		relA.relate("A", "B");
		relA.relate("B", "C");

		relB = MultimapBasedRelation.create();
		relB.relate("A", "B");
		
		relC = MultimapBasedRelation.create();
		relC.relate("A", "B");
		relC.relate("B", "C");
		relC.relate("C", "B");

		relD = MultimapBasedRelation.create();
		relD.relate("B", "A");
		relD.relate("B", "C");

		
	}
	
	@Test
	public void testCorrect() {
		assertEquals(1f, Statistics.correctness(relB, relA, universe), delta);
		assertEquals(1f, Statistics.correctness(relA, relB, universe), delta);
		assertEquals(1f, Statistics.correctness(relC, relA, universe), delta);
		assertEquals(1f, Statistics.correctness(relA, relC, universe), delta);
		assertEquals(0.5f, Statistics.correctness(relA, relD, universe), delta);

	}

	@Test
	public void testCompleteness() {
		assertEquals(0.5f, Statistics.completeness(relB, relA, universe), delta);
		assertEquals(1f, Statistics.completeness(relA, relB, universe), delta);
		assertEquals(0.5f, Statistics.completeness(relC, relA, universe), delta);
		assertEquals(1f, Statistics.completeness(relA, relC, universe), delta);

	}

}
