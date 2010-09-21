package de.faustedition.document;

import java.util.ArrayDeque;
import java.util.Arrays;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.GraphDatabaseTransactional;

@GraphDatabaseTransactional
public class DocumentResource extends ServerResource {
    public static final String PATH = "document";

    private final DocumentManager documentManager;

    private FaustURI descriptor;

    @Inject
    public DocumentResource(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    @Get
    public String overview() {
        init();
        Document document = documentManager.find(descriptor);
        if (document == null) {
            throw new IllegalArgumentException(descriptor.toString());
        }
        return Joiner.on(", " ).join(document.getSortedContents());
    }

    public void init() throws IllegalArgumentException {
        final String path = getReference().getRemainingPart().replaceAll("^/+", "").replaceAll("/+$", "");
        final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
        Preconditions.checkArgument(pathDeque.size() > 0);

        final String lastComponent = pathDeque.getLast();
        if (!lastComponent.endsWith(".xml")) {
            pathDeque.removeLast();
            pathDeque.addLast(lastComponent + ".xml");
        }
        pathDeque.addFirst(PATH);

        descriptor = new FaustURI(FaustAuthority.XML, "/" + Joiner.on("/").join(pathDeque));
    }

}
