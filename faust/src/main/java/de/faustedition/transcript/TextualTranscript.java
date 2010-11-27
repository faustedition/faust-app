package de.faustedition.transcript;

import org.goddag4j.Element;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;

public class TextualTranscript extends Transcript {

	public TextualTranscript(Node node) {
		super(node);
	}

	public TextualTranscript(GraphDatabaseService db, FaustURI source, Element root) {
		super(db, Type.TEXTUAL, source, root);
	}
	
	public void postprocess() {		
	}
	
	public void tokenize() {
		tokenize(getTrees().findRoot("tei", "text"));
	}
}
