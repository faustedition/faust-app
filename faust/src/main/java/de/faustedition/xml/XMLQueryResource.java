package de.faustedition.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Templates;
import de.faustedition.search.SearchResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/xml-query")
@Singleton
public class XMLQueryResource {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

    private enum Mode {
        XML, VALUES, FILES
    }

    private final Sources xmlStorage;
    private final Templates templates;

    @Inject
    public XMLQueryResource(Sources xmlStorage, Templates templates) {
        this.xmlStorage = xmlStorage;
        this.templates = templates;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response queryForm(@Context Request request,
                              @QueryParam("xpath") @DefaultValue("") String xpath,
                              @QueryParam("folder") @DefaultValue("transcript") String folder,
                              @QueryParam("mode") @DefaultValue("xml") String queryMode) throws IOException {

        return templates.render(request, query(xpath, folder, queryMode));
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Templates.ViewAndModel queryResults(@QueryParam("xpath") @DefaultValue("") String xpath,
                                               @QueryParam("folder") @DefaultValue("transcript") String folder,
                                               @QueryParam("mode") @DefaultValue("xml") String queryMode) {
        return query(xpath, folder, queryMode);
    }


    private Templates.ViewAndModel query(String xpath, String folder, String queryMode) {
        final List<Map<String, Object>> files = Lists.newLinkedList();
        if (!xpath.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("XPath query for '{}'", xpath);
            }

            final XPathExpression xpathExpr = XPath.compile(xpath);
            for (FaustURI uri : xmlStorage.iterate(new FaustURI(FaustAuthority.XML, "/" + folder + "/"))) {
                Map<String, Object> entry = Maps.newHashMap();
                entry.put("uri", uri.toString());
                uri.resolve("");
                org.w3c.dom.Document document;
                try {
                    document = XMLUtil.parse(xmlStorage.getInputSource(uri));
                    NodeList xpathResultNodes = (NodeList) xpathExpr.evaluate(document, XPathConstants.NODESET);
                    List<String> xpathResults = new ArrayList<String>(xpathResultNodes.getLength());
                    for (int i = 0; i < xpathResultNodes.getLength(); i++) {
                        Node node = xpathResultNodes.item(i);
                        xpathResults.add(XMLUtil.toString(node));

                    }
                    if (!xpathResults.isEmpty()) {
                        entry.put("results", xpathResults);
                    }
                } catch (Exception e) {
                    entry.put("error", e.toString());
                } finally {
                    if (entry.containsKey("results") || entry.containsKey("error"))
                        files.add(entry);
                }
            }
        }

        final Mode mode = Mode.valueOf(queryMode.toUpperCase());
        final Templates.ViewAndModel viewModel = new Templates.ViewAndModel("xml-query")
                .add("folder", folder)
                .add("xpath", xpath)
                .add("mode", mode);

        if (mode == Mode.XML || mode == Mode.FILES) {
            viewModel.add("files", files);
        } else if (mode == Mode.VALUES) {
            final Set<String> uniqueValues = Sets.newTreeSet();
            for (Map<String, Object> file : files) {
                if (file.containsKey("results")) {
                    uniqueValues.addAll((List<String>) file.get("results"));
                }
            }
            viewModel.add("values", uniqueValues.toArray());
        }

        return viewModel;
    }
}
