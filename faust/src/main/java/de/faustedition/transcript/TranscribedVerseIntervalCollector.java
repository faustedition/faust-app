package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Sets;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.TranscribedVerseIntervalRecord;
import de.faustedition.text.AnnotationStart;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.Token;
import de.faustedition.text.XML;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep3;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.map;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscribedVerseIntervalCollector extends ForwardingIterator<Token> {

    private static final Pattern VERSE_NUMBER_PATTERN = Pattern.compile("[0-9]+");

    private final Iterator<Token> delegate;
    private final Database database;
    private final long transcriptId;

    private final String xmlNameKey;
    private final String verseXmlName;
    private final String verseNumberAttribute;

    private final SortedSet<Integer> verses = Sets.newTreeSet();

    public TranscribedVerseIntervalCollector(Iterator<Token> delegate, NamespaceMapping namespaceMapping, Database database, long transcriptId) {
        this.delegate = delegate;
        this.database = database;
        this.transcriptId = transcriptId;

        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);
        this.verseXmlName = map(namespaceMapping, new QName(TEI_NS_URI, "l"));
        this.verseNumberAttribute = map(namespaceMapping, new QName(TEI_NS_URI, "n"));
    }

    @Override
    protected Iterator<Token> delegate() {
        return delegate;
    }


    @Override
    public Token next() {
        final Token token = super.next();
        if (token instanceof AnnotationStart) {
            final ObjectNode data = ((AnnotationStart) token).getData();
            if (data.path(xmlNameKey).asText().equals(verseXmlName)) {
                final Matcher verseNumberMatcher = VERSE_NUMBER_PATTERN.matcher(data.path(verseNumberAttribute).asText());
                while (verseNumberMatcher.find()) {
                    try {
                        verses.add(Integer.parseInt(verseNumberMatcher.group()));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return token;
    }

    @Override
    public boolean hasNext() {
        final boolean hasNext = super.hasNext();
        if (!hasNext && !verses.isEmpty()) {
            database.transaction(new Database.TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(DSLContext sql) throws Exception {
                    sql.delete(Tables.TRANSCRIBED_VERSE_INTERVAL)
                            .where(Tables.TRANSCRIBED_VERSE_INTERVAL.TRANSCRIPT_ID.eq(transcriptId))
                            .execute();

                    final InsertValuesStep3<TranscribedVerseIntervalRecord, Long, Integer, Integer> insertBatch = sql.insertInto(
                            Tables.TRANSCRIBED_VERSE_INTERVAL,
                            Tables.TRANSCRIBED_VERSE_INTERVAL.TRANSCRIPT_ID,
                            Tables.TRANSCRIBED_VERSE_INTERVAL.VERSE_START,
                            Tables.TRANSCRIBED_VERSE_INTERVAL.VERSE_END
                    );

                    int start = -1;
                    int next = -1;
                    for (Iterator<Integer> it = verses.iterator(); it.hasNext(); ) {
                        final Integer verse = it.next();
                        if (verse == next) {
                            next++;
                        } else if (verse > next) {
                            if (start >= 0) {
                                insertBatch.values(transcriptId, start, next);
                            }

                            start = verse;
                            next = verse + 1;
                        }

                        if (!it.hasNext() && start >= 0) {
                            insertBatch.values(transcriptId, start, verse + 1);
                        }
                    }

                    insertBatch.execute();
                    return null;
                }
            });
        }
        return hasNext;
    }
}
