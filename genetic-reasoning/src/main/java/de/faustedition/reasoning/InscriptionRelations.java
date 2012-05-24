package de.faustedition.reasoning;
import java.util.HashSet;


public class InscriptionRelations{

	public static boolean areParadigmaticallyRelated(Inscription i,
			Inscription j) {
	
		final double COMMON_RATIO = 0.4;
		
	HashSet<Integer> intersection = new HashSet<Integer>(i);
	intersection.retainAll(j);
	final int common = intersection.size();
	return (common >= COMMON_RATIO * i.size() && 
			common >= COMMON_RATIO * j.size());
	
	}
	
	public static boolean syntagmaticallyPrecedes(Inscription i, Inscription j) {
		
		double iAverage = 0;
		for (int line : i) {
			iAverage += line;
		}
		iAverage = iAverage / i.size();
		
		double jAverage = 0;
		for (int line : j) {
			jAverage += line;
		}
		jAverage = jAverage / j.size();
		
		return iAverage < jAverage;
	}
	
	public static boolean exclusivelyContains(Inscription i, Inscription j) {
		return i.first() < j.first() && i.last() > j.last();
	}
}
