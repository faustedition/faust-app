package de.faustedition.text;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.faustedition.AbstractContextTest;
import eu.interedition.text.*;
import eu.interedition.text.Text;

import org.codehaus.jackson.JsonNode;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import static eu.interedition.text.query.QueryCriteria.annotationName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SpeakerList extends AbstractContextTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void listVerses() throws IOException {
		Multimap<eu.interedition.text.Text, Layer<JsonNode>> speakers = HashMultimap.create();
		for (Layer annotation : annotationName(new Name(TextConstants.TEI_NS, "speaker")).iterate(sessionFactory.getCurrentSession())) {
			speakers.put(annotation.getTarget().getText(), annotation);
		}

		SortedSet<String> names = Sets.newTreeSet();

		for (Text text : speakers.keySet()) {
			final TreeSet<TextRange> ranges = Sets.newTreeSet();
			for (Layer<JsonNode> annotation : speakers.get(text)) {
				ranges.add(annotation.getTarget());
			}
			Iterables.addAll(names, Iterables.transform(text.read(ranges).values(), new Function<String, String>() {
				@Override
				public String apply(@Nullable String input) {
					return input
						.replaceAll("^[\\p{Punct}\\s]+", "")
						.replaceAll("[\\p{Punct}\\s]+$", "");
				}
			}));
		}

		System.out.println(Joiner.on("\n").join(names));
	}
}
