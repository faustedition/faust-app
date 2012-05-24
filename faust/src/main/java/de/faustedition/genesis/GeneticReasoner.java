package de.faustedition.genesis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.faustedition.reasoning.GeneticReasoning;
import de.faustedition.xml.XMLStorage;


@Component
public class GeneticReasoner {
	
	@Autowired
	private static XMLStorage xml;
	
	
	public static void reason() {
		
		GeneticReasoning r = new GeneticReasoning();		
		// generate inscriptions...
		//r.reason();
		
	}
}
