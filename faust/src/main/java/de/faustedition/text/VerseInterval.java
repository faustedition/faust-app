package de.faustedition.text;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VerseInterval {
	protected String name;
	protected int start;
	protected int end;

	public VerseInterval(String name, int start, int end) {
		this.name = name;
		this.start = start;
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getStart()).append(", ").append(getEnd()).append("]").toString();
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

	public static VerseInterval fromRequestAttibutes(int part, int actOrScene, int scene) throws ResourceException {
		try {
			switch (part) {
				case 0:
				case 1:
					if (actOrScene == 0) {
						return VerseInterval.ofPart(part);
					}
					return VerseInterval.ofScene(part, actOrScene);
				case 2:
					if (actOrScene == 0) {
						return VerseInterval.ofPart(part);
					}
					if (scene == 0) {
						return VerseInterval.ofAct(part, actOrScene);
					} else {
						return VerseInterval.ofScene(part, actOrScene, scene);
					}
				default:
					throw new IllegalArgumentException("Part " + part);
			}
		} catch (IllegalArgumentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		}
	}

	public static VerseInterval ofPart(int part) {
		switch (part) {
			case 0:
			case 1:
				final SortedMap<Integer, VerseInterval> scenes = (part == 0 ? PROLOGUE_SCENES : FAUST_1_SCENES);
				return new VerseInterval(
					Integer.toString(part),
					scenes.get(scenes.firstKey()).getStart(),
					scenes.get(scenes.lastKey()).getEnd()
				);
			case 2:
				final SortedMap<Integer, VerseInterval> firstAct = FAUST_2_SCENES.get(FAUST_2_SCENES.firstKey());
				final SortedMap<Integer, VerseInterval> lastAct = FAUST_2_SCENES.get(FAUST_2_SCENES.lastKey());
				return new VerseInterval(
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
		return new VerseInterval(
			String.format("%d.%d", part, act),
			scenes.get(scenes.firstKey()).getStart(),
			scenes.get(scenes.lastKey()).getEnd()
		);
	}

	public static final Comparator<VerseInterval> INTERVAL_COMPARATOR = new Comparator<VerseInterval>() {

		@Override
		public int compare(VerseInterval o1, VerseInterval o2) {
			final int startDiff = o1.start - o2.start;
			return (startDiff == 0 ? (o2.end - o1.end) : startDiff);
		}
	};

	public static final SortedMap<Integer,VerseInterval> PROLOGUE_SCENES = Maps.newTreeMap();
	public static final SortedMap<Integer,VerseInterval> FAUST_1_SCENES = Maps.newTreeMap();
	public static final SortedMap<Integer,SortedMap<Integer, VerseInterval>> FAUST_2_SCENES = Maps.newTreeMap();

	static {
		PROLOGUE_SCENES.put(1, new VerseInterval("Zueignung", 1, 33));
		PROLOGUE_SCENES.put(2, new VerseInterval("Vorspiel auf dem Theater", 33, 243));
		PROLOGUE_SCENES.put(3, new VerseInterval("Prolog im Himmel", 243, 354));

		FAUST_1_SCENES.put(1, new VerseInterval("Nacht", 354, 808));
		FAUST_1_SCENES.put(2, new VerseInterval("Vor dem Tor", 808, 1178));
		FAUST_1_SCENES.put(3, new VerseInterval("Studierzimmer I", 1178, 1530));
		FAUST_1_SCENES.put(4, new VerseInterval("Studierzimmer II", 1530, 2073));
		FAUST_1_SCENES.put(5, new VerseInterval("Auerbachs Keller in Leipzig", 2073, 2337));
		FAUST_1_SCENES.put(6, new VerseInterval("Hexenk\u00fcche", 2337, 2605));
		FAUST_1_SCENES.put(7, new VerseInterval("Straße. Faust Margarete", 2605, 2678));
		FAUST_1_SCENES.put(8, new VerseInterval("Abend. Ein kleines reinliches Zimmer", 2678, 2805));
		FAUST_1_SCENES.put(9, new VerseInterval("Spaziergang", 2805, 2865));
		FAUST_1_SCENES.put(10, new VerseInterval("Der Nachbarin Haus", 2865, 3025));
		FAUST_1_SCENES.put(11, new VerseInterval("Stra\u00dfe. Faust Mephistopheles", 3025, 3073));
		FAUST_1_SCENES.put(12, new VerseInterval("Garten", 3073, 3205));
		FAUST_1_SCENES.put(13, new VerseInterval("Ein Gartenh\u00e4uschen", 3205, 3217));
		FAUST_1_SCENES.put(14, new VerseInterval("Wald und H\u00f6hle", 3217, 3374));
		FAUST_1_SCENES.put(15, new VerseInterval("Gretchens Stube", 3374, 3414));
		FAUST_1_SCENES.put(16, new VerseInterval("Marthens Garten", 3414, 3544));
		FAUST_1_SCENES.put(17, new VerseInterval("Am Brunnen", 3544, 3587));
		FAUST_1_SCENES.put(18, new VerseInterval("Zwinger", 3587, 3620));
		FAUST_1_SCENES.put(19, new VerseInterval("Nacht. Stra\u00dfe vor Gretchens T\u00fcre", 3620, 3776));
		FAUST_1_SCENES.put(20, new VerseInterval("Dom", 3776, 3835));
		FAUST_1_SCENES.put(21, new VerseInterval("Walpurgisnacht", 3835, 4223));
		FAUST_1_SCENES.put(22, new VerseInterval("Walpurgisnachtstraum", 4223, 4399));
		// 23: Tr&uuml;ber Tag Feld, EA, S. 291-294
		FAUST_1_SCENES.put(24, new VerseInterval("Nacht, offen Feld", 4399, 4405));
		FAUST_1_SCENES.put(25, new VerseInterval("Kerker", 4405, 4613));

		final SortedMap<Integer, VerseInterval> faust2FirstActScenes = Maps.newTreeMap();
		faust2FirstActScenes.put(1, new VerseInterval("Anmutige Gegend", 4613, 4728));
		faust2FirstActScenes.put(2, new VerseInterval("Kaiserliche Pfalz", 4728, 6566));
		FAUST_2_SCENES.put(1, faust2FirstActScenes);

		final SortedMap<Integer, VerseInterval> faust2SecondActScenes = Maps.newTreeMap();
		faust2SecondActScenes.put(1, new VerseInterval("Hochgew\u00f6lbtes, enges, gothisches Zimmer", 6566, 6819));
		faust2SecondActScenes.put(2, new VerseInterval("Laboratorium", 6819, 7005));
		faust2SecondActScenes.put(3, new VerseInterval("Klassische Walpurgisnacht", 7005, 8488));
		FAUST_2_SCENES.put(2, faust2SecondActScenes);

		final SortedMap<Integer, VerseInterval> faust2ThirdActScenes = Maps.newTreeMap();
		faust2ThirdActScenes.put(1, new VerseInterval("Vor dem Pallaste des Menelas zu Sparta", 8488, 9127));
		faust2ThirdActScenes.put(2, new VerseInterval("Innerer Burghof", 9127, 9574));
		faust2ThirdActScenes.put(3, new VerseInterval("Schattiger Hain", 9574, 10039));
		FAUST_2_SCENES.put(3, faust2ThirdActScenes);

		final SortedMap<Integer, VerseInterval> faust2FourthActScenes = Maps.newTreeMap();
		faust2FourthActScenes.put(1, new VerseInterval("Hochgebirg", 10039, 10345));
		faust2FourthActScenes.put(2, new VerseInterval("Auf dem Vorgebirg", 10345, 10783));
		faust2FourthActScenes.put(3, new VerseInterval("Des Gegenkaisers Zelt", 10783, 11043));
		FAUST_2_SCENES.put(4, faust2FourthActScenes);

		final SortedMap<Integer, VerseInterval> faust2FifthActScenes = Maps.newTreeMap();
		faust2FifthActScenes.put(1, new VerseInterval("Offene Gegend", 11043, 11143));
		faust2FifthActScenes.put(2, new VerseInterval("Pallast", 11143, 11288));
		faust2FifthActScenes.put(3, new VerseInterval("Tiefe Nacht", 11288, 11511));
		faust2FifthActScenes.put(4, new VerseInterval("Gro\u00dfer Vorhof des Pallasts", 11511, 11844));
		faust2FifthActScenes.put(5, new VerseInterval("Bergschluchten", 11844, 12112));
		FAUST_2_SCENES.put(5, faust2FifthActScenes);
	}

	public boolean overlapsWith(VerseInterval other) {
		return (start < other.end) && (end > other.start);
	}
}
