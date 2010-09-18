package de.faustedition.document;

import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import de.faustedition.db.GraphDatabaseTransactional;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@GraphDatabaseTransactional
public class ArchiveResource extends ServerResource {

    private final XMLStorage xmlStorage;
    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public ArchiveResource(XMLStorage xmlStorage, TemplateRepresentationFactory viewFactory) {
        this.xmlStorage = xmlStorage;
        this.viewFactory = viewFactory;
    }

    @Get
    public Representation render() throws IOException, XPathExpressionException, SAXException {
        final Map<String, Object> model = new HashMap<String, Object>();
        final String id = (String) getRequestAttributes().get("id");
        final Document archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveManager.ARCHIVE_DESCRIPTOR_URI));

        if (id == null) {
            model.put("archives", archives.getDocumentElement());
            return viewFactory.create("document/archives", getRequest().getClientInfo(), model);
        } else {
            XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
            Element archive = new NodeListWrapper<Element>(xpathById, archives).singleResult(Element.class);
            if (archive == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
                return null;
            }
            model.put("archive", archive);
            return viewFactory.create("document/archive", getRequest().getClientInfo(), model);
        }
    }
}
