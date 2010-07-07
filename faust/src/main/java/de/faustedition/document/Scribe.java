package de.faustedition.document;

public enum Scribe {
	GOETHE("g", "Goethe"), //
	ECKERMANN("ec", "Eckermann"), //
	GEIST("gt", "Geist"), //
	GOECHHAUSEN("gh", "Goechhausen"), //
	GOETTLING("go", "Goettling"), //
	JOHN("jo", "John"), //
	KRAEUTER("kr", "Kräuter"), //
	MUELLER("m", "Müller"), //
	RIEMER("ri", "Riemer"), //
	SCHUCHARDT("st", "Schuchardt"), //
	STADELMANN("sta", "Stadelmann"), //
	HELENE_VULPIUS("v", "Helene Vulpius"), //
	WELLER_JOHN("wejo", "Weller und John"), //
	WOLFF("wo", "Pius Alexander Wolff"), //
	CONTEMPORARY("zs", "Zeitgenössische Schrift"), //
	ANY_SCRIBE("sc", "Schreiberhand"), //
	UNKNOWN_SCRIBE_1("xx", "Fremde Hand #1"), //
	UNKNOWN_SCRIBE_2("xy", "Fremde Hand #2"), //
	UNKNOWN_SCRIBE_3("xz", "Fremde Hand #3");

	private final String key;
	private final String fullname;

	private Scribe(String key, String fullname) {
		this.key = key;
		this.fullname = fullname;
	}

	public String getKey() {
		return key;
	}

	public String getFullname() {
		return fullname;
	}
}
