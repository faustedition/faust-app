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

package de.faustedition.genesis.lines;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import de.faustedition.SimpleVerseInterval;
import de.faustedition.VerseInterval;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.search.Normalization;
import de.faustedition.transcript.TranscriptManager;
import eu.interedition.text.*;
import eu.interedition.text.neo4j.LayerNode;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.codehaus.jackson.JsonNode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.ValueContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static eu.interedition.text.Query.*;

/**
 * User: moz
 * Date: 09.07.14
 * Time: 17:03
 */

@Component
@DependsOn(value = "transcriptManager")
public class VerseManager {

	private static final String INDEX_VERSE_INTERVAL = "index-verse-interval";

	private static final String INDEX_VERSE_FULLTEXT = "index-verse-text";

	private static final Pattern VERSE_NUMBER_PATTERN = Pattern.compile("[0-9]+");

	private static final Logger LOG = LoggerFactory.getLogger(VerseManager.class);

	@Autowired
	private eu.interedition.text.neo4j.Neo4jTextRepository<JsonNode> textRepository;

	@Autowired
	private FaustGraph faustGraph;

	@Autowired
	private TranscriptManager transcriptManager;

	public void register(FaustGraph faustGraph, Neo4jTextRepository<JsonNode> textRepo, MaterialUnit mu, LayerNode<JsonNode> transcript) {

		for (VerseInterval vi : registeredFor(transcript)) {
			((GraphVerseInterval)vi).node.delete();
		}
		Index<Node> verseTextIndex = faustGraph.getDb().index().forNodes(INDEX_VERSE_FULLTEXT,
				MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext") );

		final SortedSet<Long> verses = Sets.newTreeSet();
		for (Layer<JsonNode> verse : textRepo.query(and(text(transcript), name(new Name(TextConstants.TEI_NS, "l"))))) {

			//final Matcher verseNumberMatcher = VERSE_NUMBER_PATTERN.matcher(Objects.firstNonNull(verse.data().path("n").getTextValue(), ""));
			long lineNum = verse.data().get("n") != null ? verse.data().get("n").asLong(-1) : -1;

			if (lineNum >= 0) {
				verses.add(lineNum);
			}

			Anchor<JsonNode> anchor = Iterables.getOnlyElement(verse.getAnchors());
			try {
				String verseText = anchor.getText().read(anchor.getRange());
				LOG.trace("Indexing verse: " + verseText);
				verseTextIndex.add(((LayerNode)verse).node, "fulltext",
						Normalization.normalize(verseText));

			} catch (IOException e) {
				LOG.error("Error indexing line " + lineNum);
			}
		}

		Index<Node> verseIndex = faustGraph.getDb().index().forNodes(INDEX_VERSE_INTERVAL);

		long start = -1;
		long next = -1;
		for (Iterator<Long> it = verses.iterator(); it.hasNext(); ) {
			final long verse = it.next();
			if (verse == next) {
				next++;
			} else if (verse > next) {
				if (start >= 0) {
					final GraphVerseInterval vi = new GraphVerseInterval(faustGraph.getDb(), (int)start, (int)next - 1);
					vi.setTranscript(transcript);
					verseIndex.add(vi.node, "start", ValueContext.numeric(vi.getStart()));
					verseIndex.add(vi.node, "end", ValueContext.numeric(vi.getEnd()));

				}

				start = verse;
				next = verse + 1;
			}

			if (!it.hasNext() && start >= 0) {
				final GraphVerseInterval vi = new GraphVerseInterval(faustGraph.getDb(), (int)start, (int)verse);
				vi.setTranscript(transcript);
				verseIndex.add(vi.node, "start", ValueContext.numeric(vi.getStart()));
				verseIndex.add(vi.node, "end", ValueContext.numeric(vi.getEnd()));

			}
		}
		if (LOG.isDebugEnabled()) {
			// TODO LOG.debug("Registered verse intervals {} for {}", Iterables.toString(registeredFor(session, transcript)), transcript);
		}
	}

	public Iterable<VerseInterval> registeredFor(Layer<JsonNode> transcript) {

		Iterable<Relationship> relationshipsToVerseIntervals = ((LayerNode) transcript).node.getRelationships(GraphVerseInterval.VERSE_INTERVAL_IN_TRANSCRIPT_RT);
		Function<Relationship, VerseInterval> getAttachedNode = new Function<Relationship, VerseInterval>() {
			@Override
			public VerseInterval apply(@Nullable Relationship input) {
				return new GraphVerseInterval(input.getEndNode());
			}
		};
		return Iterables.transform(relationshipsToVerseIntervals, getAttachedNode);
	}

/*
	public static Iterable<TranscribedVerseInterval> all(Session session) {
		return iterateResults(session.createCriteria(TranscribedVerseInterval.class), TranscribedVerseInterval.class);
	}
*/

	public Iterable<GraphVerseInterval> forInterval(GraphVerseInterval verseInterval) {

		// find all intervals for which start < verseInterval.getEnd() and end > verseInterval.getStart()
		Index<Node> verseIndex = faustGraph.getDb().index().forNodes(INDEX_VERSE_INTERVAL);
		BooleanQuery andQuery = new BooleanQuery();
		NumericRangeQuery<Integer> startQuery = NumericRangeQuery.newIntRange("start", 0, verseInterval.getEnd(), true, true);
		NumericRangeQuery<Integer> endQuery = NumericRangeQuery.newIntRange("end", verseInterval.getStart(), Integer.MAX_VALUE, true, true);
		andQuery.add(startQuery, BooleanClause.Occur.MUST);
		andQuery.add(endQuery, BooleanClause.Occur.MUST);
		IndexHits<Node> resultNodes = verseIndex.query(andQuery);
		Function<Node, GraphVerseInterval> verseIntervalForNode = new Function<Node, GraphVerseInterval>() {
			@Override
			public GraphVerseInterval apply(@Nullable Node input) {
				return new GraphVerseInterval(input);
			}
		};
		return Iterables.transform(resultNodes, verseIntervalForNode);
	}

	public ImmutableListMultimap<MaterialUnit, GraphVerseInterval> indexByMaterialUnit(Iterable<GraphVerseInterval> verseIntervals) {

		Function materialUnitOfVerseInterval = new Function<GraphVerseInterval, MaterialUnit>() {
			@Override
			public MaterialUnit apply(@Nullable GraphVerseInterval input) {
				return transcriptManager.materialUnitForTranscript(input.getTranscript(textRepository));
			}
		};

		return Multimaps.index(verseIntervals, materialUnitOfVerseInterval);
	}

	public static SortedSet<VerseInterval> scenesOf(int part) {
		final SortedSet<VerseInterval> scenes = Sets.newTreeSet(INTERVAL_COMPARATOR);
		switch (part) {
			case 0:
				scenes.addAll(PROLOGUE_SCENES.values());
				break;
			case 1:
				scenes.addAll(FAUST_1_SCENES.values());
				break;
			case 2:
				for (SortedMap<Integer, VerseInterval> act : FAUST_2_SCENES.values()) {
					scenes.addAll(act.values());
				}
				break;
			default:
				throw new IllegalArgumentException("Part " + part);
		}
		return scenes;
	}

	public static VerseInterval fromRequestAttibutes(Map<String, Object> requestAttributes) throws ResourceException {
		try {
			final int part = Integer.parseInt((String) requestAttributes.get("part"));
			final int actOrScene = Integer.parseInt(Objects.firstNonNull((String) requestAttributes.get("act_scene"), "0"));
			final int scene = Integer.parseInt(Objects.firstNonNull((String) requestAttributes.get("scene"), "0"));

			switch (part) {
				case 0:
				case 1:
					if (actOrScene == 0) {
						return ofPart(part);
					}
					return ofScene(part, actOrScene);
				case 2:
					if (actOrScene == 0) {
						return ofPart(part);
					}
					if (scene == 0) {
						return ofAct(part, actOrScene);
					} else {
						return ofScene(part, actOrScene, scene);
					}
				default:
					throw new IllegalArgumentException("Part " + part);
			}
		} catch (IllegalArgumentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		}
	}


	public Iterable<LayerNode<JsonNode>> fulltextQuery (String queryString) {
		Preconditions.checkState(this.faustGraph.getDb().index().existsForNodes(INDEX_VERSE_FULLTEXT));
		Index<Node> verseFulltextIndex = this.faustGraph.getDb().index().forNodes(INDEX_VERSE_FULLTEXT);

		// construct a fuzzy query
		String normalizedQueryString = Normalization.normalize(queryString);
		ArrayList<String> tokens = Lists.newArrayList(normalizedQueryString.split(" "));
		Function<String, SpanMultiTermQueryWrapper> stringToQuery = new Function<String, SpanMultiTermQueryWrapper>() {
			@Override
			public SpanMultiTermQueryWrapper apply(@Nullable String input) {
				Term term = new Term("fulltext", input.toLowerCase());
				FuzzyQuery fuzzyQuery = new FuzzyQuery(term);
				SpanMultiTermQueryWrapper spanMultiTermQueryWrapper = new SpanMultiTermQueryWrapper(fuzzyQuery);
				return spanMultiTermQueryWrapper;
			}
		};

		SpanMultiTermQueryWrapper[] clauses =
				(Lists.newArrayList(Iterables.transform(tokens, stringToQuery)).toArray(new SpanMultiTermQueryWrapper[tokens.size()]));

		SpanNearQuery query = new SpanNearQuery(clauses, 5, false);
		IndexHits<Node> verseResults = verseFulltextIndex.query(query);

		Function <Node, LayerNode<JsonNode>> wrapLayerNodes = new Function<Node, LayerNode<JsonNode>>() {
			@Override
			public LayerNode apply(@Nullable Node input) {
				return new LayerNode<JsonNode>(textRepository, input);
			}
		};

		return Iterables.transform(verseResults, wrapLayerNodes);
	}


	public static VerseInterval ofPart(int part) {
		switch (part) {
			case 0:
			case 1:
				final SortedMap<Integer, VerseInterval> scenes = (part == 0 ? PROLOGUE_SCENES : FAUST_1_SCENES);
				return new SimpleVerseInterval(
						Integer.toString(part),
						scenes.get(scenes.firstKey()).getStart(),
						scenes.get(scenes.lastKey()).getEnd()
				);
			case 2:
				final SortedMap<Integer, VerseInterval> firstAct = FAUST_2_SCENES.get(FAUST_2_SCENES.firstKey());
				final SortedMap<Integer, VerseInterval> lastAct = FAUST_2_SCENES.get(FAUST_2_SCENES.lastKey());
				return new SimpleVerseInterval(
						Integer.toString(part),
						firstAct.get(firstAct.firstKey()).getStart(),
						lastAct.get(lastAct.lastKey()).getEnd()
				);
			default:
				throw new IllegalArgumentException("Part " + part);
		}
	}

	public static VerseInterval ofScene(int part, int act, int scene) {
		Preconditions.checkArgument(part == 2, "Part " + part);
		Preconditions.checkArgument(FAUST_2_SCENES.containsKey(act), "Act " + act);
		final SortedMap<Integer, VerseInterval> scenes = FAUST_2_SCENES.get(act);
		Preconditions.checkArgument(scenes.containsKey(scene), "Scene " + scene);
		return scenes.get(scene);
	}

	public static VerseInterval ofScene(int part, int scene) {
		Preconditions.checkArgument(part == 0 || part == 1, "Part " + part);
		final SortedMap<Integer, VerseInterval> scenes = (part == 0 ? PROLOGUE_SCENES : FAUST_1_SCENES);
		Preconditions.checkArgument(scenes.containsKey(scene), "Scene " + scene);
		return scenes.get(scene);
	}

	public static VerseInterval ofAct(int part, int act) {
		Preconditions.checkArgument(part == 2, "Part " + part);
		Preconditions.checkArgument(FAUST_2_SCENES.containsKey(act), "Act " + act);

		final SortedMap<Integer, VerseInterval> scenes = FAUST_2_SCENES.get(act);
		return new SimpleVerseInterval(
				String.format("%d.%d", part, act),
				scenes.get(scenes.firstKey()).getStart(),
				scenes.get(scenes.lastKey()).getEnd()
		);
	}

	public static final Comparator<VerseInterval> INTERVAL_COMPARATOR = new Comparator<VerseInterval>() {

		@Override
		public int compare(VerseInterval o1, VerseInterval o2) {
			final int startDiff = o1.getStart() - o2.getStart();
			return (startDiff == 0 ? (o2.getEnd() - o1.getEnd()) : startDiff);
		}
	};

	public static final SortedMap<Integer,VerseInterval> PROLOGUE_SCENES = Maps.newTreeMap();
	public static final SortedMap<Integer,VerseInterval> FAUST_1_SCENES = Maps.newTreeMap();
	public static final SortedMap<Integer,SortedMap<Integer, VerseInterval>> FAUST_2_SCENES = Maps.newTreeMap();

	static {
		PROLOGUE_SCENES.put(1, new SimpleVerseInterval("Zueignung", 1, 33));
		PROLOGUE_SCENES.put(2, new SimpleVerseInterval("Vorspiel auf dem Theater", 33, 243));
		PROLOGUE_SCENES.put(3, new SimpleVerseInterval("Prolog im Himmel", 243, 354));

		FAUST_1_SCENES.put(1, new SimpleVerseInterval("Nacht", 354, 808));
		FAUST_1_SCENES.put(2, new SimpleVerseInterval("Vor dem Tor", 808, 1178));
		FAUST_1_SCENES.put(3, new SimpleVerseInterval("Studierzimmer I", 1178, 1530));
		FAUST_1_SCENES.put(4, new SimpleVerseInterval("Studierzimmer II", 1530, 2073));
		FAUST_1_SCENES.put(5, new SimpleVerseInterval("Auerbachs Keller in Leipzig", 2073, 2337));
		FAUST_1_SCENES.put(6, new SimpleVerseInterval("Hexenk\u00fcche", 2337, 2605));
		FAUST_1_SCENES.put(7, new SimpleVerseInterval("Straße. Faust Margarete", 2605, 2678));
		FAUST_1_SCENES.put(8, new SimpleVerseInterval("Abend. Ein kleines reinliches Zimmer", 2678, 2805));
		FAUST_1_SCENES.put(9, new SimpleVerseInterval("Spaziergang", 2805, 2865));
		FAUST_1_SCENES.put(10, new SimpleVerseInterval("Der Nachbarin Haus", 2865, 3025));
		FAUST_1_SCENES.put(11, new SimpleVerseInterval("Stra\u00dfe. Faust Mephistopheles", 3025, 3073));
		FAUST_1_SCENES.put(12, new SimpleVerseInterval("Garten", 3073, 3205));
		FAUST_1_SCENES.put(13, new SimpleVerseInterval("Ein Gartenh\u00e4uschen", 3205, 3217));
		FAUST_1_SCENES.put(14, new SimpleVerseInterval("Wald und H\u00f6hle", 3217, 3374));
		FAUST_1_SCENES.put(15, new SimpleVerseInterval("Gretchens Stube", 3374, 3414));
		FAUST_1_SCENES.put(16, new SimpleVerseInterval("Marthens Garten", 3414, 3544));
		FAUST_1_SCENES.put(17, new SimpleVerseInterval("Am Brunnen", 3544, 3587));
		FAUST_1_SCENES.put(18, new SimpleVerseInterval("Zwinger", 3587, 3620));
		FAUST_1_SCENES.put(19, new SimpleVerseInterval("Nacht. Stra\u00dfe vor Gretchens T\u00fcre", 3620, 3776));
		FAUST_1_SCENES.put(20, new SimpleVerseInterval("Dom", 3776, 3835));
		FAUST_1_SCENES.put(21, new SimpleVerseInterval("Walpurgisnacht", 3835, 4223));
		FAUST_1_SCENES.put(22, new SimpleVerseInterval("Walpurgisnachtstraum", 4223, 4399));
		// 23: Tr&uuml;ber Tag Feld, EA, S. 291-294
		FAUST_1_SCENES.put(24, new SimpleVerseInterval("Nacht, offen Feld", 4399, 4405));
		FAUST_1_SCENES.put(25, new SimpleVerseInterval("Kerker", 4405, 4613));

		final SortedMap<Integer, VerseInterval> faust2FirstActScenes = Maps.newTreeMap();
		faust2FirstActScenes.put(1, new SimpleVerseInterval("Anmutige Gegend", 4613, 4728));
		faust2FirstActScenes.put(2, new SimpleVerseInterval("Kaiserliche Pfalz", 4728, 6566));
		FAUST_2_SCENES.put(1, faust2FirstActScenes);

		final SortedMap<Integer, VerseInterval> faust2SecondActScenes = Maps.newTreeMap();
		faust2SecondActScenes.put(1, new SimpleVerseInterval("Hochgew\u00f6lbtes, enges, gothisches Zimmer", 6566, 6819));
		faust2SecondActScenes.put(2, new SimpleVerseInterval("Laboratorium", 6819, 7005));
		faust2SecondActScenes.put(3, new SimpleVerseInterval("Klassische Walpurgisnacht", 7005, 8488));
		FAUST_2_SCENES.put(2, faust2SecondActScenes);

		final SortedMap<Integer, VerseInterval> faust2ThirdActScenes = Maps.newTreeMap();
		faust2ThirdActScenes.put(1, new SimpleVerseInterval("Vor dem Pallaste des Menelas zu Sparta", 8488, 9127));
		faust2ThirdActScenes.put(2, new SimpleVerseInterval("Innerer Burghof", 9127, 9574));
		faust2ThirdActScenes.put(3, new SimpleVerseInterval("Schattiger Hain", 9574, 10039));
		FAUST_2_SCENES.put(3, faust2ThirdActScenes);

		final SortedMap<Integer, VerseInterval> faust2FourthActScenes = Maps.newTreeMap();
		faust2FourthActScenes.put(1, new SimpleVerseInterval("Hochgebirg", 10039, 10345));
		faust2FourthActScenes.put(2, new SimpleVerseInterval("Auf dem Vorgebirg", 10345, 10783));
		faust2FourthActScenes.put(3, new SimpleVerseInterval("Des Gegenkaisers Zelt", 10783, 11043));
		FAUST_2_SCENES.put(4, faust2FourthActScenes);

		final SortedMap<Integer, VerseInterval> faust2FifthActScenes = Maps.newTreeMap();
		faust2FifthActScenes.put(1, new SimpleVerseInterval("Offene Gegend", 11043, 11143));
		faust2FifthActScenes.put(2, new SimpleVerseInterval("Pallast", 11143, 11288));
		faust2FifthActScenes.put(3, new SimpleVerseInterval("Tiefe Nacht", 11288, 11511));
		faust2FifthActScenes.put(4, new SimpleVerseInterval("Gro\u00dfer Vorhof des Pallasts", 11511, 11844));
		faust2FifthActScenes.put(5, new SimpleVerseInterval("Bergschluchten", 11844, 12112));
		FAUST_2_SCENES.put(5, faust2FifthActScenes);
	}

}
