package de.faustedition.transcript;

import static org.restlet.data.MediaType.APPLICATION_XML;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.io.GoddagJsonSerializer;
import org.juxtasoftware.goddag.io.GoddagXMLReader;
import org.restlet.data.Form;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;

import de.faustedition.JsonRespresentation;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.xml.CustomNamespaceMap;
import de.faustedition.xml.XMLUtil;

@GraphDatabaseTransactional
public class TranscriptResource extends ServerResource {
    private Transcript transcript;

    @Inject
    public TranscriptResource() {
        super();
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    @Get("json")
    public Representation streamJson() {
        return new JsonRespresentation() {

            @Override
            protected void generate() throws IOException {
                new GoddagJsonSerializer().serialize(generator, CustomNamespaceMap.INSTANCE, transcript);
            }
        };
    }

    @Get("xml")
    public Representation streamXML() {
        final Form parameters = getRequest().getResourceRef().getQueryAsForm();
        final String rootQName = parameters.getFirstValue("root");

        String rootPrefix = null;
        String rootLocalName = null;
        final int lcColonIndex = rootQName == null ? -1 : rootQName.indexOf(':');
        if (lcColonIndex > 0 && (lcColonIndex + 1) < rootQName.length()) {
            rootPrefix = rootQName.substring(0, lcColonIndex);
            rootLocalName = rootQName.substring(lcColonIndex + 1);
        }

        Element rootCandidate = null;
        if (rootPrefix != null && rootLocalName != null) {
            rootCandidate = transcript.findRoot(rootPrefix, rootLocalName);
        } else {
            Iterator<Element> rootIt = transcript.iterator();
            rootCandidate = (rootIt.hasNext() ? rootIt.next() : null);
        }

        if (rootCandidate == null) {
            return null;
        }

        final Element root = rootCandidate;
        final boolean showTextNodes = Boolean.valueOf(parameters.getFirstValue("textnodes", "false"));
        return new OutputRepresentation(APPLICATION_XML) {

            @Override
            public void write(OutputStream outputStream) throws IOException {
                try {
                    Transformer transformer = XMLUtil.transformerFactory().newTransformer();
                    Source source = new GoddagXMLReader(root, CustomNamespaceMap.INSTANCE, showTextNodes).getSAXSource();
                    transformer.transform(source, new StreamResult(outputStream));
                } catch (TransformerException e) {
                    throw new IOException(e);
                }
            }
        };
    }
}
