package de.faustedition.reasoning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import de.faustedition.reasoning.Inscription;
import de.faustedition.reasoning.InscriptionRelations;

public class InscriptionRelationTest {

	private Inscription inscriptionA;
	private Inscription inscriptionB;
	private Inscription inscriptionC;
	private Inscription inscriptionD;
	
	@org.junit.Before
	public void setup() {
		this.inscriptionA = new Inscription(Arrays.asList(
				5, 6, 7));
		this.inscriptionB = new Inscription(Arrays.asList(
				6, 7, 8));
		this.inscriptionC = new Inscription(Arrays.asList(
				8, 9, 10));
		this.inscriptionD = new Inscription(Arrays.asList(
				4, 9, 10));

	}

	@Test
	public void testAreParadigmaticallyRelated() {
		assertTrue(InscriptionRelations.areParadigmaticallyRelated(
						this.inscriptionA, this.inscriptionB));
		assertFalse(InscriptionRelations.areParadigmaticallyRelated(
				this.inscriptionB, this.inscriptionC));
	}
	
	@Test
	public void testSyntagmaticallyPrecedes() {
		assertTrue(InscriptionRelations.syntagmaticallyPrecedes(
				this.inscriptionA, this.inscriptionB));
		assertTrue(InscriptionRelations.syntagmaticallyPrecedes(
				this.inscriptionA, this.inscriptionC));
		assertTrue(InscriptionRelations.syntagmaticallyPrecedes(
				this.inscriptionB, this.inscriptionC));

		assertFalse(InscriptionRelations.syntagmaticallyPrecedes(
				this.inscriptionC, this.inscriptionC));
		assertFalse(InscriptionRelations.syntagmaticallyPrecedes(
				this.inscriptionC, this.inscriptionA));
	}
	
	@Test
	public void testExclusivelyContains() {
		assertTrue(InscriptionRelations.exclusivelyContains(
				this.inscriptionD, this.inscriptionA));
		assertFalse(InscriptionRelations.exclusivelyContains(
				this.inscriptionB, this.inscriptionA));
		
		
	}

	
}
