package de.faustedition.transcript;

import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import org.goddag4j.Element;
import org.goddag4j.token.LineTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.TokenizerUtil;

public class TextualGoddagTranscript extends GoddagTranscript {

	public TextualGoddagTranscript(Node node) {
		super(node);
	}

	public TextualGoddagTranscript(GraphDatabaseService db, FaustURI source, Element root) {
		super(db, TranscriptType.TEXTUAL, source, root);
	}

	public void postprocess() {
	}

	@Override
	public void tokenize() {
		super.tokenize();
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new LineTokenMarkupGenerator(), "f", "lines");
	}
	
	@Override
	public Element getDefaultRoot() {
		return getTrees().getRoot(TEI_NS_PREFIX, "text");
	}
}
