/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.transcript;

import static eu.interedition.text.Query.and;
import static eu.interedition.text.Query.name;
import static eu.interedition.text.Query.text;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.JsonNode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.annotations.Index;
import org.hibernate.criterion.Restrictions;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.faustedition.VerseInterval;
import de.faustedition.document.MaterialUnit;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Entity
@Table(name = "faust_transcribed_verse_interval")
public class TranscribedVerseInterval extends VerseInterval {

	private static final Logger LOG = LoggerFactory.getLogger(TranscribedVerseInterval.class);
	private static final Pattern VERSE_NUMBER_PATTERN = Pattern.compile("[0-9]+");

	private long id;
	private Transcript transcript;

	@Id
	@GeneratedValue
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Transient
	@Override
	public String getName() {
		return super.getName();
	}

	@ManyToOne
	@JoinColumn(name = "transcript_id", nullable = false)
	public Transcript getTranscript() {
		return transcript;
	}

	public void setTranscript(Transcript transcript) {
		this.transcript = transcript;
	}


	@Index(name = "verse_start_end", columnNames = {"verse_start, verse_end"})
	@Column(name = "verse_start", nullable = false)
	public int getStart() {
		return super.getStart();
	}

	public void setStart(int start) {
		super.setStart(start);
	}

	@Column(name="verse_end", nullable = false)
	public int getEnd() {
		return super.getEnd();
	}

	public void setEnd(int end) {
		super.setEnd(end);
	}

	public static void register(Session session, TextRepository<JsonNode> textRepo, Transcript transcript) {
		for (TranscribedVerseInterval vi : registeredFor(session, transcript)) {
			session.delete(vi);
		}

		final SortedSet<Integer> verses = Sets.newTreeSet();
		for (Layer<JsonNode> verse : textRepo.query(and(text(transcript.getText()), name(new Name(TextConstants.TEI_NS, "l"))))) {
			final Matcher verseNumberMatcher = VERSE_NUMBER_PATTERN.matcher(Objects.firstNonNull(verse.data().path("n").getTextValue(), ""));
			while (verseNumberMatcher.find()) {
				try {
					verses.add(Integer.parseInt(verseNumberMatcher.group()));
				} catch (NumberFormatException e) {
				}
			}
		}

		int start = -1;
		int next = -1;
		for (Iterator<Integer> it = verses.iterator(); it.hasNext(); ) {
			final Integer verse = it.next();
			if (verse == next) {
				next++;
			} else if (verse > next) {
				if (start >= 0) {
					final TranscribedVerseInterval vi = new TranscribedVerseInterval();
					vi.setTranscript(transcript);
					vi.setStart(start);
					vi.setEnd(next);
					session.save(vi);
				}

				start = verse;
				next = verse + 1;
			}

			if (!it.hasNext() && start >= 0) {
				final TranscribedVerseInterval vi = new TranscribedVerseInterval();
				vi.setTranscript(transcript);
				vi.setStart(start);
				vi.setEnd(verse + 1);
				session.save(vi);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Registered verse intervals {} for {}", Iterables.toString(registeredFor(session, transcript)), transcript);
		}
	}

	public static <T> Iterable<T> iterateResults(final Criteria c, Class<T> type) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new AbstractIterator<T>() {

					final ScrollableResults results = c.scroll(ScrollMode.FORWARD_ONLY);

					@Override
					@SuppressWarnings("unchecked")
					protected T computeNext() {
						return (results.next() ? (T) results.get()[0]: endOfData());
					}
				};
			}
		};
	}


	public static Iterable<TranscribedVerseInterval> registeredFor(Session session, Transcript transcript) {
		return iterateResults(
				session.createCriteria(TranscribedVerseInterval.class).add(Restrictions.eq("transcript", transcript)), TranscribedVerseInterval.class
				);
	}

	public static Iterable<TranscribedVerseInterval> all(Session session) {
		return iterateResults(session.createCriteria(TranscribedVerseInterval.class), TranscribedVerseInterval.class);
	}

	public static Iterable<TranscribedVerseInterval> forInterval(Session session, VerseInterval verseInterval) {
		return iterateResults(session.createCriteria(TranscribedVerseInterval.class)
			.add(Restrictions.lt("start", verseInterval.getEnd()))
			.add(Restrictions.gt("end", verseInterval.getStart())),
			TranscribedVerseInterval.class);
	}

	public static ImmutableListMultimap<MaterialUnit, TranscribedVerseInterval> indexByMaterialUnit(final GraphDatabaseService db, Iterable<TranscribedVerseInterval> intervals) {
		return Multimaps.index(intervals, new Function<TranscribedVerseInterval, MaterialUnit>() {

			@Override
			public MaterialUnit apply(@Nullable TranscribedVerseInterval input) {
				final Node materialUnitNode = db.getNodeById(input.getTranscript().getMaterialUnitId());
				return MaterialUnit.forNode(materialUnitNode);
			}
		});
	}
}
