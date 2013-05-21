package de.faustedition.transcript;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.TranscribedVerseIntervalRecord;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.document.MaterialUnit;
import de.faustedition.text.VerseInterval;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;
import org.codehaus.jackson.JsonNode;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep3;
import org.jooq.Record;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.interedition.text.Query.and;
import static eu.interedition.text.Query.name;
import static eu.interedition.text.Query.text;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscribedVerseInterval {

	private static final Logger LOG = LoggerFactory.getLogger(TranscribedVerseInterval.class);

	private static final Pattern VERSE_NUMBER_PATTERN = Pattern.compile("[0-9]+");

    public static Map<MaterialUnit, Collection<VerseInterval>> byMaterialUnit(final DataSource dataSource, final GraphDatabaseService graphDb, final int from, final int to) {
        return Maps.newHashMap();
    }

	public static void register(DSLContext db, TextRepository<JsonNode> textRepo, TranscriptRecord transcript) {
        // delete existing intervals
        db.delete(Tables.TRANSCRIBED_VERSE_INTERVAL)
                .where(Tables.TRANSCRIBED_VERSE_INTERVAL.TRANSCRIPT_ID.eq(transcript.getId()))
                .execute();

		final SortedSet<Integer> verses = Sets.newTreeSet();
		for (Layer<JsonNode> verse : textRepo.query(and(text(textRepo.findByIdentifier(transcript.getTextId())), name(new Name(TextConstants.TEI_NS, "l"))))) {
			final Matcher verseNumberMatcher = VERSE_NUMBER_PATTERN.matcher(Objects.firstNonNull(verse.data().path("n").getTextValue(), ""));
			while (verseNumberMatcher.find()) {
				try {
					verses.add(Integer.parseInt(verseNumberMatcher.group()));
				} catch (NumberFormatException e) {
				}
			}
		}

        InsertValuesStep3<TranscribedVerseIntervalRecord,Long,Integer,Integer> insertBatch = db.insertInto(
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
                    insertBatch.values(transcript.getId(), start, next);
				}

				start = verse;
				next = verse + 1;
			}

			if (!it.hasNext() && start >= 0) {
                insertBatch.values(transcript.getId(), start, verse + 1);
			}
		}

        final int count = insertBatch.execute();

        if (LOG.isDebugEnabled()) {
			LOG.debug("Registered verse {} interval(s) for {}", count, transcript.getSourceUri());
		}
	}
}
