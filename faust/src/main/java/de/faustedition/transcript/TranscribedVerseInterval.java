package de.faustedition.transcript;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import de.faustedition.VerseInterval;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.util.SQL;
import org.hibernate.Session;
import org.hibernate.annotations.Index;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.interedition.text.query.QueryCriteria.and;
import static eu.interedition.text.query.QueryCriteria.annotationName;
import static eu.interedition.text.query.QueryCriteria.text;

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

	public static void register(Session session, Transcript transcript) {
		for (TranscribedVerseInterval vi : registeredFor(session, transcript)) {
			session.delete(vi);
		}

		final SortedSet<Integer> verses = Sets.newTreeSet();
		for (Annotation verse : and(text(transcript.getText()), annotationName(new Name(TextConstants.TEI_NS, "l"))).iterate(session)) {
			final Matcher verseNumberMatcher = VERSE_NUMBER_PATTERN.matcher(Objects.firstNonNull(verse.getData().path("n").getValueAsText(), ""));
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

	public static Iterable<TranscribedVerseInterval> registeredFor(Session session, Transcript transcript) {
		return SQL.iterate(session.createCriteria(TranscribedVerseInterval.class).add(Restrictions.eq("transcript", transcript)), TranscribedVerseInterval.class);
	}

	public static Iterable<TranscribedVerseInterval> all(Session session) {
		return SQL.iterate(session.createCriteria(TranscribedVerseInterval.class), TranscribedVerseInterval.class);
	}

	public static Iterable<TranscribedVerseInterval> forInterval(Session session, VerseInterval verseInterval) {
		return SQL.iterate(session.createCriteria(TranscribedVerseInterval.class)
			.add(Restrictions.lt("start", verseInterval.getEnd()))
			.add(Restrictions.gt("end", verseInterval.getStart())),
			TranscribedVerseInterval.class);
	}
}
