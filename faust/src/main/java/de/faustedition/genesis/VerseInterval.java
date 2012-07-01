package de.faustedition.genesis;

import com.google.common.collect.Maps;

import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VerseInterval {
	protected String name;
	protected int start;
	protected int end;

	public VerseInterval() {
	}

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

	public static final SortedMap<Integer,VerseInterval> PROLOGUE_SCENES = Maps.newTreeMap();
	public static final SortedMap<Integer,VerseInterval> FAUST_1_SCENES = Maps.newTreeMap();

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


	}
}
