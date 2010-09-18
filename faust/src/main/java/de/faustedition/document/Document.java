package de.faustedition.document;

import java.net.URI;

import org.neo4j.graphdb.Node;

import de.faustedition.db.GraphDatabaseRoot;

public class Document extends MaterialUnit {
    private static final String PREFIX = GraphDatabaseRoot.PREFIX + ".document";

    public Document(Node node) {
        super(node);
    }

    public Document(Node node, URI uri) {
        this(node);
        setUri(uri);
    }

    public URI getUri() {
        return URI.create((String) getUnderlyingNode().getProperty(PREFIX + ".uri"));
    }

    public void setUri(URI uri) {
        getUnderlyingNode().setProperty(PREFIX + ".uri", uri.toString());
    }

}
