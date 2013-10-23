package de.faustedition.transcript;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.document.DocumentRequested;
import de.faustedition.text.AnnotationEnd;
import de.faustedition.text.AnnotationStart;
import de.faustedition.text.Characters;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.Token;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_SIG_GE_URI;
import static de.faustedition.text.TokenPredicates.name;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class TranscriptSegments extends AbstractScheduledService {


    public static enum Unit {
        PAGE, LINE;
    }

    private final Database database;
    private final Transcripts transcripts;

    private final Predicate<Token> documentaryPage;
    private final Predicate<Token> documentaryLine;

    private final Predicate<Token> textualPage;
    private final Predicate<Token> textualLine;


    @Inject
    public TranscriptSegments(Database database, EventBus eventBus, Transcripts transcripts, NamespaceMapping namespaceMapping) {
        this.database = database;
        this.transcripts = transcripts;

        this.documentaryPage = name(namespaceMapping, new QName(TEI_SIG_GE_URI, "document"));
        this.documentaryLine = name(namespaceMapping, new QName(TEI_SIG_GE_URI, "line"));

        this.textualPage = name(namespaceMapping, new QName(TEI_NS_URI, "page"));
        this.textualLine = Predicates.or(
                name(namespaceMapping, new QName(TEI_NS_URI, "l")),
                name(namespaceMapping, new QName(TEI_NS_URI, "stage")),
                name(namespaceMapping, new QName(TEI_NS_URI, "speaker")),
                name(namespaceMapping, new QName(TEI_NS_URI, "head")),
                name(namespaceMapping, new QName(TEI_NS_URI, "p"))
        );

        eventBus.register(this);
    }

    @Subscribe
    public void event(final DocumentRequested documentRequested) {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                update(sql, documentRequested.getId());
                return null;
            }
        });
    }

    public void update(DSLContext sql, final long documentId) {
        final boolean documentSegmentsExist = (sql.selectCount()
                .from(Tables.DOCUMENT_SEGMENT)
                .where(Tables.DOCUMENT_SEGMENT.DOCUMENT_ID.eq(documentId))
                .fetchOne().value1() > 0);
        if (documentSegmentsExist) {
            return;
        }

        final Map<Transcripts.Type, SegmentCollector> segments = Maps.newHashMap();
        segments.put(
                Transcripts.Type.DOCUMENTARY,
                transcripts.documentary(documentId, new SegmentCollector(documentaryPage, documentaryLine))
        );
        segments.put(
                Transcripts.Type.TEXTUAL,
                transcripts.textual(documentId, new SegmentCollector(textualPage, textualLine))
        );

        final BatchBindStep batch = sql.batch(sql.insertInto(
                Tables.DOCUMENT_SEGMENT,
                Tables.DOCUMENT_SEGMENT.DOCUMENT_ID,
                Tables.DOCUMENT_SEGMENT.TRANSCRIPT_TYPE,
                Tables.DOCUMENT_SEGMENT.SEGMENT_UNIT,
                Tables.DOCUMENT_SEGMENT.SEGMENT_ORDER,
                Tables.DOCUMENT_SEGMENT.SEGMENT_START,
                Tables.DOCUMENT_SEGMENT.SEGMENT_END
        ).values((Long) null, null, null, null, null, null));

        for (Map.Entry<Transcripts.Type, SegmentCollector> segmentType : segments.entrySet()) {
            final SegmentCollector segmentCollector = segmentType.getValue();

            int order = 0;
            for (Range<Long> page : segmentCollector.pages) {
                batch.bind(
                        documentId,
                        segmentType.getKey().ordinal(),
                        Unit.PAGE.ordinal(),
                        order++,
                        page.lowerEndpoint(),
                        page.upperEndpoint()
                );
            }

            order = 0;
            for (Range<Long> line : segmentCollector.lines) {
                batch.bind(
                        documentId,
                        segmentType.getKey().ordinal(),
                        Unit.LINE.ordinal(),
                        order++,
                        line.lowerEndpoint(),
                        line.upperEndpoint()
                );
            }

        }
        batch.execute();
    }

    private static class SegmentCollector implements Transcripts.TokenCallback<SegmentCollector> {

        private final Predicate<Token> pagePredicate;
        private final Predicate<Token> linePredicate;

        private final List<Range<Long>> pages = Lists.newLinkedList();
        private final List<Range<Long>> lines = Lists.newLinkedList();

        private SegmentCollector(Predicate<Token> pagePredicate, Predicate<Token> linePredicate) {
            this.pagePredicate = pagePredicate;
            this.linePredicate = linePredicate;
        }

        @Override
        public SegmentCollector withTokens(Iterator<Token> tokens) throws Exception {
            long offset = 0;
            String pageStartId = "";
            String lineStartId = "";
            long pageStartOffset = 0;
            long lineStartOffset = 0;
            while (tokens.hasNext()) {
                final Token token = tokens.next();
                if (token instanceof AnnotationStart) {
                    final AnnotationStart annotationStart = (AnnotationStart) token;
                    if (pagePredicate.apply(annotationStart)) {
                        pageStartId = annotationStart.getId();
                        pageStartOffset = offset;
                    } else if (linePredicate.apply(annotationStart)) {
                        lineStartId = annotationStart.getId();
                        lineStartOffset = offset;
                    }
                } else if (token instanceof AnnotationEnd) {
                    final String annotationId = ((AnnotationEnd) token).getId();
                    if (pageStartId.equals(annotationId)) {
                        pages.add(Range.closedOpen(pageStartOffset, offset));
                    } else if (lineStartId.equals(annotationId)) {
                        lines.add(Range.closedOpen(lineStartOffset, offset));
                    }
                } else if (token instanceof Characters) {
                    offset += ((Characters) token).getContent().length();
                }
            }
            return this;

        }
    }

    @Override
    protected void runOneIteration() throws Exception {
        // FIXME: pre-flight segments
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.DAYS);
    }
}
