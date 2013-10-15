package de.faustedition.document;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.graph.Graph;
import de.faustedition.http.HTTP;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Deque;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DocumentPage {

    private final Document document;
    private final int page;


    public static DocumentPage fromPath(String path, Graph graph) throws WebApplicationException {
        final Deque<String> pathDeque = HTTP.pathDeque(path);

        int page;
        try {
            page = Integer.parseInt(Objects.firstNonNull(pathDeque.pollLast(), "1"));
            pathDeque.removeLast();
        } catch (NumberFormatException e) {
            page = 1;
        }

        pathDeque.addFirst("document");
        final FaustURI documentUri = new FaustURI(FaustAuthority.XML, HTTP.joinPath(pathDeque));
        final Document document = Document.findBySource(graph.db(), documentUri);
        if (document == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(documentUri.toString()).build());
        }

        return new DocumentPage(document, page);
    }

    public DocumentPage(Document document, int page) {
        this.document = document;
        this.page = page;
    }

    public Document getDocument() {
        return document;
    }

    public int getPage() {
        return page;
    }

    public MaterialUnit materialUnit() {
        final SortedSet<MaterialUnit> contents = document.getSortedContents();
        final int pages = contents.size();
        if (pages < page) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Request for page " + page + "; there are " + pages + " pages.").build());
        }
        return Iterables.get(contents, page - 1);
    }
}
