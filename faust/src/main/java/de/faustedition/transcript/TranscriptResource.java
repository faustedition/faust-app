package de.faustedition.transcript;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.APPLICATION_XML;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.io.GoddagJsonSerializer;
import org.juxtasoftware.goddag.io.GoddagXMLReader;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.transcript.Transcript.Type;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLUtil;

@GraphDatabaseTransactional
public class TranscriptResource extends ServerResource {
    public static final String PATH = "transcript";

    private final TranscriptManager transcriptManager;
    private Type transcriptType;
    private String rootPrefix;
    private String rootLocalName;
    private FaustURI source;
    private boolean goddagTextNodes;

    @Inject
    public TranscriptResource(TranscriptManager transcriptManager) {
        this.transcriptManager = transcriptManager;
    }

    @Get("json")
    public Representation streamJson() {
        init();
        final Transcript transcript = transcriptManager.find(source, transcriptType);
        if (transcript == null) {
            throw new IllegalArgumentException(source + "[" + transcriptType + "]");
        }
        return new OutputRepresentation(APPLICATION_JSON) {

            @Override
            public void write(OutputStream outputStream) throws IOException {
                setCharacterSet(CharacterSet.UTF_8);
                final JsonGenerator generator = new JsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
                new GoddagJsonSerializer().serialize(generator, transcript);
            }
        };
    }

    @Get("xml")
    public Representation streamXML() {
        init();
        if (rootPrefix == null || rootLocalName == null) {
            throw new IllegalArgumentException();
        }
        final Transcript transcript = transcriptManager.find(source, transcriptType);
        if (transcript == null) {
            throw new IllegalArgumentException(source + "[" + transcriptType + "]");
        }

        final Element root = transcript.findRoot(rootPrefix, rootLocalName);
        if (root == null) {
            throw new IllegalArgumentException(Element.getQName(rootPrefix, rootLocalName));
        }

        return new OutputRepresentation(APPLICATION_XML) {

            @Override
            public void write(OutputStream outputStream) throws IOException {
                try {
                    Transformer transformer = XMLUtil.transformerFactory().newTransformer();
                    Source source = new GoddagXMLReader(root, CustomNamespaceMap.INSTANCE, goddagTextNodes).getSAXSource();
                    transformer.transform(source, new StreamResult(outputStream));
                } catch (TransformerException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public void init() throws IllegalArgumentException {
        final String path = getReference().getRemainingPart().replaceAll("^/+", "").replaceAll("/+$", "");
        final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
        Preconditions.checkArgument(pathDeque.size() > 1);

        transcriptType = Transcript.Type.valueOf(pathDeque.removeFirst().toUpperCase());

        String lastComponent = pathDeque.getLast();
        final int lcColonIndex = lastComponent.indexOf(':');
        if (lcColonIndex > 0 && (lcColonIndex + 1) < lastComponent.length()) {
            rootPrefix = lastComponent.substring(0, lcColonIndex);
            rootLocalName = lastComponent.substring(lcColonIndex + 1);
            pathDeque.removeLast();
        }

        lastComponent = pathDeque.getLast();
        if (!lastComponent.endsWith(".xml")) {
            pathDeque.removeLast();
            pathDeque.addLast(lastComponent + ".xml");
        }
        pathDeque.addFirst(PATH);

        source = new FaustURI(FaustAuthority.XML, "/" + Joiner.on("/").join(pathDeque));
        
        final Form parameters = getReference().getQueryAsForm();
        goddagTextNodes = Boolean.valueOf(parameters.getFirstValue("textmarkup", "false"));
    }
}
