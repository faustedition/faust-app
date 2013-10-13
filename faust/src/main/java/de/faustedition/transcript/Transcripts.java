package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import de.faustedition.FaustURI;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.document.MaterialUnit;
import de.faustedition.text.AnnotationProcessor;
import de.faustedition.text.ElementContextFilter;
import de.faustedition.text.LineBreaker;
import de.faustedition.text.MilestoneMarkupProcessor;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.Token;
import de.faustedition.text.WhitespaceCompressor;
import de.faustedition.text.XML;
import de.faustedition.text.XMLStreamToTokenFunction;
import de.faustedition.xml.Sources;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.util.Iterator;

import static de.faustedition.text.TokenPredicates.name;

@Singleton
public class Transcripts {

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


	public TranscriptRecord transcriptOf(final MaterialUnit materialUnit) throws IOException, XMLStreamException {
        final FaustURI source = materialUnit.getTranscriptSource();
        if (source == null) {
            return null;
        }

        return database.transaction(new Database.TransactionCallback<TranscriptRecord>() {
            @Override
            public TranscriptRecord doInTransaction(DSLContext sql) throws Exception {
                TranscriptRecord transcriptRecord = sql.selectFrom(Tables.TRANSCRIPT).fetchOne();
                if (transcriptRecord != null) {
                    return transcriptRecord;
                }

                transcriptRecord = sql.newRecord(Tables.TRANSCRIPT);
                transcriptRecord.setSourceUri(source.toString());
                transcriptRecord.store();

                final long transcriptId = transcriptRecord.getId();
                final XMLInputFactory xif = XML.inputFactory();
                XMLEventReader reader = null;
                try {
                    Iterator<Token> tokens = Iterators.transform(
                            XML.stream(reader = xif.createXMLEventReader(new SAXSource(xml.getInputSource(source)))),
                            new XMLStreamToTokenFunction(objectMapper, namespaceMapping)
                    );

                    tokens = new TranscriptMarkupHandler(tokens, objectMapper, namespaceMapping);

                    tokens = Iterators.filter(tokens, new ElementContextFilter(
                            Predicates.or(
                                    name(namespaceMapping, "tei:teiHeader"),
                                    name(namespaceMapping, "tei:front"),
                                    name(namespaceMapping, "tei:app"),
                                    name(namespaceMapping, "tei:fw")
                            ),
                            Predicates.or(
                                    name(namespaceMapping, "tei:lem")
                            )

                    ));

                    tokens = new WhitespaceCompressor(tokens, namespaceMapping, Predicates.or(
                            name(namespaceMapping, "tei:text"),
                            name(namespaceMapping, "tei:div"),
                            name(namespaceMapping, "tei:lg"),
                            name(namespaceMapping, "tei:subst"),
                            name(namespaceMapping, "tei:choice"),
                            name(namespaceMapping, "tei:zone")
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
                            name(namespaceMapping, "ge:document")
                    ));

                    tokens = new MilestoneMarkupProcessor(tokens, objectMapper, namespaceMapping);

                    tokens = new TranscribedVerseIntervalCollector(tokens, namespaceMapping, database, transcriptId);

                    // FIXME: read text and annotations and batch store
                    tokens = new AnnotationProcessor(tokens);
                } finally {
                    XML.closeQuietly(reader);
                }

                return transcriptRecord;
            }
        });
    }
}
