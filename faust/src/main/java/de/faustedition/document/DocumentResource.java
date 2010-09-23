package de.faustedition.document;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.DocumentaryTranscript;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;

@GraphDatabaseTransactional
public class DocumentResource extends ServerResource {
    public static final String PATH = "document";

    private final DocumentManager documentManager;

    private FaustURI descriptor;

    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public DocumentResource(DocumentManager documentManager, TemplateRepresentationFactory viewFactory) {
        this.documentManager = documentManager;
        this.viewFactory = viewFactory;
    }

    @Get("html")
    public Representation overview() throws IOException {
        init();
        Document document = documentManager.find(descriptor);
        if (document == null) {
            throw new IllegalArgumentException(descriptor.toString());
        }

        Map<String, Object> viewModel = new HashMap<String, Object>();
        viewModel.put("document", document);
        viewModel.put("contents", document.getSortedContents());

        return viewFactory.create("document/document", getRequest().getClientInfo(), viewModel);
    }

    @Get("json")
    public Representation documentStructure() {
        init();
        final Document document = documentManager.find(descriptor);
        if (document == null) {
            throw new IllegalArgumentException(descriptor.toString());
        }
        return new DocumentJsonRespresentation(document);
    }

    public void init() throws IllegalArgumentException {
        final String path = getReference().getRelativeRef().getPath().replaceAll("^/+", "").replaceAll("/+$", "");
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

    private class DocumentJsonRespresentation extends JsonRespresentation {

        private final Document document;

        private DocumentJsonRespresentation(Document document) {
            super();
            this.document = document;
        }

        @Override
        protected void generate() throws IOException {
            generate(document);
        }

        protected void generate(MaterialUnit unit) throws IOException {
            generator.writeStartObject();
            
            generator.writeStringField("type", unit.getType().name().toLowerCase());
            generator.writeNumberField("order", unit.getOrder());
            
            final Transcript transcript = unit.getTranscript();
            if (transcript != null) {
                generator.writeObjectFieldStart("transcript");
                generator.writeStringField("type", transcript.getType().name().toLowerCase());
                generator.writeStringField("source", transcript.getSource().toString());
                if (transcript.getType() == Type.DOCUMENTARY) {
                    DocumentaryTranscript dt = (DocumentaryTranscript) transcript;
                    generator.writeArrayFieldStart("facsimiles");
                    for (FaustURI facsimile : dt.getFacsimileReferences()) {
                        generator.writeString(facsimile.toString());
                    }
                    generator.writeEndArray();
                }
                generator.writeEndObject();
            }
            
            generator.writeArrayFieldStart("contents");
            for (MaterialUnit content : new TreeSet<MaterialUnit>(unit)) {
                generate(content);
            }
            generator.writeEndArray();
            
            generator.writeEndObject();
        }
    }
}
