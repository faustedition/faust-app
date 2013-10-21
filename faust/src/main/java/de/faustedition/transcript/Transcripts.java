package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.Database;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.db.Tables;
import de.faustedition.text.ElementContextFilter;
import de.faustedition.text.LineBreaker;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TEIMilestoneMarkupProcessor;
import de.faustedition.text.Token;
import de.faustedition.text.WhitespaceCompressor;
import de.faustedition.text.XML;
import de.faustedition.text.XMLStreamToTokenFunction;
import de.faustedition.xml.Sources;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.text.TokenPredicates.name;

@Singleton
public class Transcripts {

    public static enum Type {
        TEXTUAL, DOCUMENTARY
    };

    private static final Logger LOG = Logger.getLogger(Transcripts.class.getName());

    private final Database database;
    private final Sources xml;
    private final ObjectMapper objectMapper;
    private final NamespaceMapping namespaceMapping;

    @Inject
    public Transcripts(Database database, Sources xml, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.database = database;
        this.xml = xml;
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
    }

    public <R> R textual(final long documentId, TokenCallback<R> callback) {
        final List<Long> textualTranscript = database.transaction(new Database.TransactionCallback<List<Long>>() {
            @Override
            public List<Long> doInTransaction(DSLContext sql) throws Exception {
                Record1<Long> transcriptRecord = sql.select(Tables.TRANSCRIPT.ID).from(Tables.TRANSCRIPT)
                        .join(Tables.MATERIAL_UNIT).on(Tables.TRANSCRIPT.MATERIAL_UNIT_ID.eq(Tables.MATERIAL_UNIT.ID))
                        .join(Tables.DOCUMENT).on(Tables.DOCUMENT.ID.eq(Tables.MATERIAL_UNIT.DOCUMENT_ID))
                        .where(Tables.DOCUMENT.ID.eq(documentId))
                        .and(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.eq(0))
                        .fetchOne();
                return (transcriptRecord == null ? Collections.<Long>emptyList() : Arrays.asList(transcriptRecord.value1()));
            }
        });

        return tokens(textualTranscript, callback);
    }

    public <R> R documentary(final long documentId, TokenCallback<R> callback) {
        final List<Long> documentaryTranscripts = database.transaction(new Database.TransactionCallback<List<Long>>() {
            @Override
            public List<Long> doInTransaction(DSLContext sql) throws Exception {
                final Result<Record1<Long>> idResult = sql.select(Tables.TRANSCRIPT.ID).from(Tables.TRANSCRIPT)
                        .join(Tables.MATERIAL_UNIT).on(Tables.MATERIAL_UNIT.ID.eq(Tables.TRANSCRIPT.MATERIAL_UNIT_ID))
                        .join(Tables.DOCUMENT).on(Tables.DOCUMENT.ID.eq(Tables.MATERIAL_UNIT.DOCUMENT_ID))
                        .where(Tables.DOCUMENT.ID.eq(documentId))
                        .and(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.gt(0))
                        .orderBy(Tables.MATERIAL_UNIT.DOCUMENT_ORDER)
                        .fetch();

                final List<Long> ids = Lists.newLinkedList();
                for (Record1<Long> id : idResult) {
                    ids.add(id.value1());
                }
                return ids;
            }
        });

        return tokens(documentaryTranscripts, callback);
    }

    public <R> R tokens(final Collection<Long> ids, final TokenCallback<R> callback) {
        final Map<Long, FaustURI> uris = database.transaction(new Database.TransactionCallback<Map<Long, FaustURI>>() {
            @Override
            public Map<Long, FaustURI> doInTransaction(DSLContext sql) throws Exception {
                final Result<Record2<Long, String>> uriMappings = sql
                        .select(Tables.TRANSCRIPT.ID, Tables.TRANSCRIPT.SOURCE_URI)
                        .from(Tables.TRANSCRIPT)
                        .where(Tables.TRANSCRIPT.ID.in(ids))
                        .fetch();
                final Map<Long, FaustURI> uris = Maps.newHashMap();
                for (Record2<Long, String> uriMapping : uriMappings) {
                    uris.put(uriMapping.value1(), new FaustURI(FaustAuthority.XML, "/" + uriMapping.value2()));
                }
                return uris;
            }
        });

        try {
            return callback.withTokens(transcriptTokens(new AbstractIterator<Token>() {

                Iterator<Long> idIt = ids.iterator();
                Iterator<Token> tokenIt = Iterators.emptyIterator();
                XMLEventReader xmlEvents = null;

                @Override
                protected Token computeNext() {
                    while (!tokenIt.hasNext() && idIt.hasNext()) {
                        final Long id = idIt.next();
                        final FaustURI uri = uris.get(id);
                        if (uri == null) {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("Cannot find source for transcript #" + id);
                            }
                            continue;
                        }
                        if (!xml.isResource(uri)) {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("Source " + uri + " for transcript #" + id + " does not exist");
                            }
                            continue;
                        }
                        try {
                            XML.closeQuietly(xmlEvents);
                            xmlEvents = XML.inputFactory().createXMLEventReader(new StreamSource(xml.file(uri)));
                            tokenIt = Iterators.transform(
                                    XML.stream(xmlEvents),
                                    new XMLStreamToTokenFunction(objectMapper, namespaceMapping).withNodePath()
                            );
                        } catch (XMLStreamException e) {
                            XML.closeQuietly(xmlEvents);
                            throw Throwables.propagate(e);
                        }
                    }
                    if (!tokenIt.hasNext()) {
                        XML.closeQuietly(xmlEvents);
                        return endOfData();
                    }
                    return tokenIt.next();
                }

                @Override
                protected void finalize() throws Throwable {
                    XML.closeQuietly(xmlEvents);
                }
            }));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Iterator<Token> transcriptTokens(Iterator<Token> tokens) {
        tokens = Iterators.filter(tokens, new ElementContextFilter(
                Predicates.or(
                        name(namespaceMapping, "tei:teiHeader"),
                        name(namespaceMapping, "tei:front"),
                        name(namespaceMapping, "tei:app")
                ),
                Predicates.or(
                        name(namespaceMapping, "tei:lem")
                )

        ));

        tokens = new WhitespaceCompressor(tokens, namespaceMapping, Predicates.or(
                name(namespaceMapping, "tei:body"),
                name(namespaceMapping, "tei:group"),
                name(namespaceMapping, "tei:text"),
                name(namespaceMapping, "tei:div"),
                name(namespaceMapping, "tei:lg"),
                name(namespaceMapping, "tei:sp"),
                name(namespaceMapping, "tei:subst"),
                name(namespaceMapping, "tei:choice"),
                name(namespaceMapping, "tei:surface"),
                name(namespaceMapping, "tei:zone"),
                name(namespaceMapping, "ge:document"),
                name(namespaceMapping, "f:overw")
        ));

        tokens = new LineBreaker(tokens, Predicates.or(
                name(namespaceMapping, "tei:text"),
                name(namespaceMapping, "tei:div"),
                name(namespaceMapping, "tei:head"),
                name(namespaceMapping, "tei:sp"),
                name(namespaceMapping, "tei:stage"),
                name(namespaceMapping, "tei:speaker"),
                name(namespaceMapping, "tei:lg"),
                name(namespaceMapping, "tei:l"),
                name(namespaceMapping, "tei:p"),
                name(namespaceMapping, "tei:ab"),
                name(namespaceMapping, "tei:line"),
                name(namespaceMapping, "ge:line")
        ));

        tokens = new TEIMilestoneMarkupProcessor(tokens, objectMapper, namespaceMapping);

        tokens = new TranscriptMarkupHandler(tokens, objectMapper, namespaceMapping);

        return tokens;
    }

    public static interface TokenCallback<R> {

        R withTokens(Iterator<Token> tokens) throws Exception;

    }
}
