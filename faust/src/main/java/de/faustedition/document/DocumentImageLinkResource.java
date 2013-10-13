package de.faustedition.document;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Templates;
import de.faustedition.facsimile.InternetImageServer;
import de.faustedition.graph.Graph;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.awt.Dimension;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/document/imagelink/{path: .+?}")
@Singleton
public class DocumentImageLinkResource {

    private static final Logger LOG = Logger.getLogger(DocumentImageLinkResource.class.getName());

    private final Graph graph;
    private final XMLStorage xml;
	private final Templates templates;
	private final InternetImageServer imageServer;

    @Inject
    public DocumentImageLinkResource(Graph graph, XMLStorage xml, Templates templates, InternetImageServer imageServer) {
        this.graph = graph;
        this.xml = xml;
        this.templates = templates;
        this.imageServer = imageServer;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response page(@PathParam("path") final String path, @Context final Request request) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(Graph graph) throws Exception {
                final DocumentPage documentPage = DocumentPage.fromPath(path, graph);
                return templates.render(new Templates.ViewAndModel("document/imagelink")
                        .add("pageNum", documentPage.getPage())
                        .add("document", documentPage.getDocument())
                        .add("facsimileUrl", imageServer.uriFor(documentPage.materialUnit().getFacsimile())
                                .queryParam("SDS", "0,90")
                                .queryParam("CNT", "1.0")
                                .queryParam("WID", "800")
                                .queryParam("QLT", "90")
                                .queryParam("CVT", "jpeg")
                                .build().toString()),
                        request);
            }
        });
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> readLinkData(@PathParam("path") final String path) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<Map<String, Object>>() {
            @Override
            public Map<String, Object> doInTransaction(Graph graph) throws Exception {
                final DocumentPage documentPage = DocumentPage.fromPath(path, graph);
                final FaustURI transcriptURI = documentPage.materialUnit().getTranscriptSource();
                final org.w3c.dom.Document source = XMLUtil.parse(xml.getInputSource(transcriptURI));
                return DocumentImageLinks.read(source, null);
            }
        });
    }

    @GET
    @Produces(DocumentImageLinks.IMAGE_SVG_TYPE)
    public Source readLinkMap(@PathParam("path") final String path) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<Source>() {
            @Override
            public Source doInTransaction(Graph graph) throws Exception {
                final DocumentPage documentPage = DocumentPage.fromPath(path, graph);
                final MaterialUnit page = documentPage.materialUnit();
                final FaustURI transcriptURI = page.getTranscriptSource();

                final org.w3c.dom.Document transcript = XMLUtil.parse(xml.getInputSource(transcriptURI));

                final URI linkDataURI = DocumentImageLinks.readLinkDataURI(transcript);
                org.w3c.dom.Document svg;
                if (linkDataURI == null) {
                    LOG.fine(transcriptURI + " doesn't have image-text links yet");

                    final Dimension dimension = imageServer.dimensionOf(page.getFacsimile());

                    svg = XMLUtil.documentBuilder().newDocument();

                    final Element root = (Element) svg.appendChild(svg.createElementNS(Namespaces.SVG_NS_URI, "svg"));
                    root.setAttribute("width", Long.toString(Math.round(dimension.getWidth())));
                    root.setAttribute("height", Long.toString(Math.round(dimension.getHeight())));

                    final Node g = root.appendChild(svg.createElementNS(Namespaces.SVG_NS_URI, "g"));
                    g.appendChild(svg.createElementNS(Namespaces.SVG_NS_URI, "title")).setTextContent("Layer 1");
                } else {
                    LOG.fine(transcriptURI + " has image-text links, loading");
                    svg = XMLUtil.parse(xml.getInputSource(new FaustURI(linkDataURI)));

                    // adjust the links
                    DocumentImageLinks.read(transcript, svg);
                }

                // FIXME what if the image size changes? Change the size of the SVG?
                // Scale it? Leave it alone?
                return new DOMSource(svg);
            }
        });
    }

    @PUT
    @Consumes(DocumentImageLinks.IMAGE_SVG_TYPE)
	public String write(@PathParam("path") final String path, final InputStream svgStream) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<String>() {
            @Override
            public String doInTransaction(Graph graph) throws Exception {
                final FaustURI transcriptURI = DocumentPage.fromPath(path, graph).materialUnit().getTranscriptSource();

                final org.w3c.dom.Document svg = XMLUtil.parse(svgStream);
                final org.w3c.dom.Document source = XMLUtil.parse(xml.getInputSource(transcriptURI));

                boolean hasSourceChanged = DocumentImageLinks.write(source, svg);
                if (hasSourceChanged) {
                    LOG.fine("Added new xml:ids to " + transcriptURI);
                }


                // Check if the transcript has image links attached
                URI linkDataURI = DocumentImageLinks.readLinkDataURI(source);
                if (linkDataURI == null) {
                    // generate random URI
                    linkDataURI = new FaustURI(FaustAuthority.XML, "/image-text-links/" + UUID.randomUUID() + ".svg").toURI();

                    LOG.fine("Adding new image-text-link to " + transcriptURI);
                    DocumentImageLinks.writeLinkDataURI(source, linkDataURI);
                    hasSourceChanged = true;
                }

                // write the image links file
                LOG.fine("Writing image-text-link data to " + linkDataURI);
                xml.put(new FaustURI(linkDataURI), svg);

                if (hasSourceChanged) {
                    // write the modified transcript
                    LOG.fine("Writing " + transcriptURI);
                    xml.put(transcriptURI, source);
                }

                // FIXME
                return "<svg></svg>";
            }
        });
	}
}
