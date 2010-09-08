package de.faustedition.document;

import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpression;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import de.faustedition.db.XmlStorage;
import de.faustedition.template.TemplateRepresentationFactory;

public class ArchiveResource extends ServerResource {

    private final XmlStorage xmlStorage;
    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public ArchiveResource(XmlStorage xmlStorage, TemplateRepresentationFactory viewFactory) {
        this.xmlStorage = xmlStorage;
        this.viewFactory = viewFactory;
    }

    @Get
    public Representation render() throws IOException {
        final Map<String, Object> model = new HashMap<String, Object>();
        final String id = (String) getRequestAttributes().get("id");
        final Document archives = xmlStorage.getDocument("archives.xml");

        if (id == null) {
            model.put("archives", archives.getDocumentElement());
            return viewFactory.create("document/archives", getRequest().getClientInfo(), model);
        } else {
            XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
            Element archive = singleResult(xpathById, archives, Element.class);
            if (archive == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
                return null;
            }
            model.put("archive", archive);
            return viewFactory.create("document/archive", getRequest().getClientInfo(), model);
        }
    }
}
