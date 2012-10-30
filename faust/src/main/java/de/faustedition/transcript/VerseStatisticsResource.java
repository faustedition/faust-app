package de.faustedition.transcript;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.*;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.VerseInterval;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VerseStatisticsResource extends ServerResource {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private GraphDatabaseService graphDb;

	@Autowired
	private JsonRepresentationFactory jsonRepresentationFactory;
	private ImmutableMap<MaterialUnit,Collection<TranscribedVerseInterval>> verseStatistics;
	private int from;
	private int to;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		from = Math.max(0, Integer.parseInt((String) getRequestAttributes().get("from")));
		to = Math.max(from, Integer.parseInt((String) getRequestAttributes().get("to")));
		if (from >= to) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid interval");
		}

		final Session session = sessionFactory.getCurrentSession();
		verseStatistics = TranscribedVerseInterval.indexByMaterialUnit(graphDb, TranscribedVerseInterval.forInterval(session, new VerseInterval(null, from, to))).asMap();
	}

	@Get("json")
	public Representation chartData() {
		final List<Map<String, Object>> chartData = Lists.newLinkedList();
		final ImmutableMap<String, MaterialUnit> documentIndex = Maps.uniqueIndex(verseStatistics.keySet(), new Function<MaterialUnit, String>() {
			@Override
			public String apply(@Nullable MaterialUnit input) {				
				return input.toString() + " [" + input.node.getId() + "]";
			}
		});
		for (String documentDesc : Ordering.natural().immutableSortedCopy(documentIndex.keySet())) {
			final List<Map<String, Object>> intervals = Lists.newLinkedList();
			for (TranscribedVerseInterval interval : Ordering.from(VerseInterval.INTERVAL_COMPARATOR).immutableSortedCopy(verseStatistics.get(documentIndex.get(documentDesc)))) {
				intervals.add(new ModelMap()
					.addAttribute("start", Math.max(from, interval.getStart()))
					.addAttribute("end", Math.min(to, interval.getEnd()))
				);
			}
			chartData.add(new ModelMap()
				.addAttribute("sigil", documentDesc.substring(0,documentDesc.indexOf('[') ))
				/*.addAttribute("transcript", documentIndex.get(documentDesc).node.getId())*/
				.addAttribute("source", ((Document)documentIndex.get(documentDesc)).getSource().toString())
				.addAttribute("intervals", intervals));
		}
		return jsonRepresentationFactory.map(chartData, false);
	}
}
