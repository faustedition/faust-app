package de.faustedition.transcript;

import java.util.SortedSet;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;

public class DocumentaryTranscript extends Transcript {
    public DocumentaryTranscript(Node node) {
        super(node);
    }

    public DocumentaryTranscript(Node node, FaustURI source, SortedSet<FaustURI> facsimileReferences) {
        super(node, Type.DOCUMENTARY, source);
        setFacsimileReferences(facsimileReferences);
    }

    public void setFacsimileReferences(SortedSet<FaustURI> facsimileReferences) {
        String[] uris = new String[facsimileReferences.size()];
        int uc = 0;
        for (FaustURI uri : facsimileReferences) {
            uris[uc++] = uri.toString();
        }
        getUnderlyingNode().setProperty(PREFIX + ".documentary.facsimiles", uris);
    }

    public SortedSet<FaustURI> getFacsimileReferences() {
        SortedSet<FaustURI> facsimileReferences = new TreeSet<FaustURI>();
        for (String uri : ((String[]) getUnderlyingNode().getProperty(PREFIX + ".documentary.facsimiles"))) {
            facsimileReferences.add(FaustURI.parse(uri));
        }
        return facsimileReferences;
    }
}
