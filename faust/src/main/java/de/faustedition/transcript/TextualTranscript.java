package de.faustedition.transcript;

import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;

public class TextualTranscript extends Transcript {

    public TextualTranscript(Node node) {
        super(node);
    }

    public TextualTranscript(Node node, FaustURI source) {
        super(node, Type.TEXTUAL, source);
    }
}
