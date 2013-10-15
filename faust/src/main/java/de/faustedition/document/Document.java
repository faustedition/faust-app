package de.faustedition.document;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import de.faustedition.FaustURI;
import de.faustedition.genesis.MacrogeneticRelationManager;
import de.faustedition.graph.Graph;
import de.faustedition.graph.NodeWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import java.net.URI;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import static org.neo4j.graphdb.Direction.OUTGOING;

public class Document extends MaterialUnit {
    private static final String PREFIX = Graph.PREFIX + ".document";

    private static final String SOURCE_KEY = PREFIX + ".source";
    private static final String URI_KEY = PREFIX + "uri";
    private static final String URI_PART_KEY = PREFIX + "uri-part";
    private static final String CALLNUMBER_KEY = METADATA_PREFIX + "callnumber";
    private static final String WA_ID_KEY = METADATA_PREFIX + "wa-id";

    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9]");

    public static final String GENETIC_SOURCE_PROPERTY = "genetic-source";

    public Document(Node node) {
        super(node);
    }

    public Document(Node node, Type type, FaustURI source) {
        super(node, type);
        setSource(source);
    }

    public FaustURI getSource() {
        return new FaustURI(URI.create((String) node.getProperty(SOURCE_KEY)));
    }

    public void setSource(FaustURI uri) {
        node.setProperty(SOURCE_KEY, uri.toString());
    }

    /**
     * @param geneticSource filter; can be null
     * @return
     */
    public Set<Document> geneticallyRelatedTo(FaustURI geneticSource /*, RelationshipType type*/) {
        RelationshipType type = MacrogeneticRelationManager.TEMP_PRE_REL;
        final Iterable<Relationship> relationships = node.getRelationships(type, OUTGOING);

        final Set<Document> result = new HashSet<Document>();

        for (Relationship relationship : relationships) {
            if (geneticSource != null && relationship.getProperty(GENETIC_SOURCE_PROPERTY).equals(geneticSource.toString())) {
                final Document document = NodeWrapper.newInstance(Document.class, relationship.getEndNode());
                result.add(document);
            }
        }
        return result;
    }

    public static Document findBySource(GraphDatabaseService db, FaustURI source) {
        final Node node = db.index().forNodes(SOURCE_KEY).get(SOURCE_KEY, source).getSingle();
        return (node == null ? null : new Document(node));
    }

    public static Document findByUri(GraphDatabaseService db, FaustURI uri) {
        try {
            final Node node = db.index().forNodes(URI_KEY).get(URI_KEY, uri).getSingle();
            return (node == null ? null : new Document(node));
        } catch (NoSuchElementException e) {
            return null;
        }
    }


    public static Iterable<Document> find(GraphDatabaseService db, String id) {
        final BooleanQuery query = new BooleanQuery();
        query.add(new WildcardQuery(new Term(CALLNUMBER_KEY, id)), BooleanClause.Occur.SHOULD);
        query.add(new WildcardQuery(new Term(WA_ID_KEY, id)), BooleanClause.Occur.SHOULD);
        query.add(new WildcardQuery(new Term(URI_PART_KEY, id)), BooleanClause.Occur.SHOULD);

        return Iterables.transform(
                db.index().forNodes(PREFIX + "id").query(query),
                newWrapperFunction(Document.class));
    }

    public void index() {
        final IndexManager indexManager = node.getGraphDatabase().index();

        indexManager.forNodes(SOURCE_KEY).add(node, SOURCE_KEY, getSource());

        final Index<Node> idIndex = indexManager.forNodes(PREFIX + "id");

        for (String uri : Objects.firstNonNull(getMetadata("uri"), new String[0])) {

            try {
                indexManager.forNodes(URI_KEY).add(node, URI_KEY, new FaustURI(new URI(uri)));
            } catch (Exception e) {
                // TODO error logging
                //logger.error("error!", e);
            }
            try {
                idIndex.add(node, URI_PART_KEY, uri.substring("faust://document/".length()).toLowerCase());
            } catch (IndexOutOfBoundsException e) {
                //do nothing
            }

        }


        for (String callnumber : Objects.firstNonNull(getMetadata("callnumber"), new String[0])) {
            idIndex.add(node, CALLNUMBER_KEY, callnumber.toLowerCase());
        }
        for (String waId : Objects.firstNonNull(getMetadata("wa-id"), new String[0])) {
            if (ALPHA_NUMERIC_PATTERN.matcher(waId).find()) {
                idIndex.add(node, WA_ID_KEY, waId.toLowerCase());
            }
        }
    }
}
