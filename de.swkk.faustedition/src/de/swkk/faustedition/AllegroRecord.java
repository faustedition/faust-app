package de.swkk.faustedition;

import java.util.TreeMap;

public class AllegroRecord extends TreeMap<String, String> implements Comparable<AllegroRecord> {

	private static final long serialVersionUID = -6599507353471653744L;

	public int getId() {
		try {
			return Integer.parseInt(get("xx0"));
		} catch (Exception e) {
			return 0;
		}
	}

	public int compareTo(AllegroRecord o) {
		return getId() - o.getId();
	}
}
