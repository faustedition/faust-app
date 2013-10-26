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
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TextAnnotationEnd;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextContent;
import de.faustedition.text.TextToken;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_SIG_GE_URI;
import static de.faustedition.text.TextTokenPredicates.xmlName;
import static de.faustedition.transcript.TEIMilestoneMarkupProcessor.teiMilestone;

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

    private final Predicate<TextToken> documentaryPage;
    private final Predicate<TextToken> documentaryLine;

    private final Predicate<TextToken> textualPage;
    private final Predicate<TextToken> textualLine;


    @Inject
    public TranscriptSegments(Database database, EventBus eventBus, Transcripts transcripts, NamespaceMapping namespaceMapping) {
        this.database = database;
        this.transcripts = transcripts;

        this.documentaryPage = xmlName(namespaceMapping, new QName(TEI_SIG_GE_URI, "document"));
        this.documentaryLine = xmlName(namespaceMapping, new QName(TEI_SIG_GE_URI, "line"));

        this.textualPage = teiMilestone(namespaceMapping, "page");
        this.textualLine = Predicates.or(
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "l")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "stage")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "speaker")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "head")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "p"))
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
                new SegmentCollector(documentaryPage, documentaryLine).collect(transcripts.documentary(documentId))
        );
        segments.put(
                Transcripts.Type.TEXTUAL,
                new SegmentCollector(textualPage, textualLine).collect(transcripts.textual(documentId))
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

    private static class SegmentCollector {

        private final Predicate<TextToken> pagePredicate;
        private final Predicate<TextToken> linePredicate;

        private final List<Range<Long>> pages = Lists.newLinkedList();
        private final List<Range<Long>> lines = Lists.newLinkedList();

        private SegmentCollector(Predicate<TextToken> pagePredicate, Predicate<TextToken> linePredicate) {
            this.pagePredicate = pagePredicate;
            this.linePredicate = linePredicate;
        }

        private SegmentCollector collect(Transcript transcript) {
            long offset = 0;
            String pageStartId = "";
            String lineStartId = "";
            long pageStartOffset = 0;
            long lineStartOffset = 0;
            for (TextToken token : transcript) {
                if (token instanceof TextAnnotationStart) {
                    final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                    if (pagePredicate.apply(annotationStart)) {
                        pageStartId = annotationStart.getId();
                        pageStartOffset = offset;
                    } else if (linePredicate.apply(annotationStart)) {
                        lineStartId = annotationStart.getId();
                        lineStartOffset = offset;
                    }
                } else if (token instanceof TextAnnotationEnd) {
                    final String annotationId = ((TextAnnotationEnd) token).getId();
                    if (pageStartId.equals(annotationId)) {
                        pages.add(Range.closedOpen(pageStartOffset, offset));
                    } else if (lineStartId.equals(annotationId)) {
                        lines.add(Range.closedOpen(lineStartOffset, offset));
                    }
                } else if (token instanceof TextContent) {
                    offset += ((TextContent) token).getContent().length();
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
