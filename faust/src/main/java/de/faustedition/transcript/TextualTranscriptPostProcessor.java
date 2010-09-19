package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.util.DefaultGoddagEventHandler;
import org.juxtasoftware.goddag.util.MultiplexingGoddagEventHandler;
import org.neo4j.graphdb.GraphDatabaseService;

public class TextualTranscriptPostProcessor extends MultiplexingGoddagEventHandler implements Runnable {

    private final TextualTranscript transcript;
    private Element source;
    private GraphDatabaseService db;

    public TextualTranscriptPostProcessor(TextualTranscript transcript) {
        super();
        this.transcript = transcript;
        this.source = transcript.getRoot(TEI_NS_PREFIX, "text");
        this.db = transcript.getUnderlyingNode().getGraphDatabase();
        setHandlers(new ModificationViewHandler());
    }

    @Override
    public void run() {
        source.stream(source, this);
    }

    private class ModificationViewHandler extends DefaultGoddagEventHandler {
    }
}
