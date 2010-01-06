package de.faustedition.model.document;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CASE;

import java.util.regex.Pattern;

import org.w3c.dom.Node;

import de.faustedition.model.tei.EncodedDocument;

public enum TranscriptionStatus {
	EMPTY(null), RAW("Rohzustand"), ENCODED("kodiert"), FINISHED("abgeschlossen"), PROOF_READ_1("1. korrekturlesen"), PROOF_READ_2(
			"2. korrekturlesen"), FINALIZED("fertiggestellt");

	private Pattern regexp;

	private TranscriptionStatus(String regexpPattern) {
		if (regexpPattern != null) {
			this.regexp = Pattern.compile(Pattern.quote(regexpPattern), CASE_INSENSITIVE | UNICODE_CASE);
		}
	}

	public static TranscriptionStatus extract(EncodedDocument teiDocument) {
		TranscriptionStatus status = EMPTY;
		for (Node changeElement : teiDocument.xpath("/:TEI/:teiHeader/:revisionDesc/:change")) {
			String textContent = changeElement.getTextContent();
			for (TranscriptionStatus candidate : values()) {
				if ((candidate.regexp != null) && candidate.regexp.matcher(textContent).matches()) {
					status = candidate;
				}
			}
		}
		return status;
	}
}
