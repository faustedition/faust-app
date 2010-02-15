package de.swkk.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

public class AllegroRecordSet extends TreeSet<AllegroRecord> {

	public void parse(Resource recordSetResource) throws IOException {
		clear();
		BufferedReader recordSetReader = new BufferedReader(new InputStreamReader(recordSetResource.getInputStream(), "UTF-8"));
		AllegroRecord record = null;
		String lastEntry = null;
		do {
			String recordLine = recordSetReader.readLine();
			if (recordLine == null || !StringUtils.hasText(recordLine)) {
				if (record != null) {
					add(record);
					record = null;
					lastEntry = null;
				}

				if (recordLine == null) {
					break;
				} else {
					continue;
				}
			}

			if (record == null) {
				record = new AllegroRecord();
			}

			if (!recordLine.startsWith("#")) {
				if (lastEntry == null) {
					throw new IllegalStateException("Continuation of record line in empty record");
				}

				record.put(lastEntry, record.get(lastEntry) + recordLine);
				continue;
			}

			String recordIdentifier = StringUtils.trimWhitespace(recordLine.substring(1, Math.min(recordLine.length(), 4)));
			String recordValue = StringUtils.trimWhitespace(recordLine.substring(Math.min(recordLine.length(), 4)));

			if (record.containsKey(recordIdentifier)) {
				throw new IllegalStateException(String.format("Duplicate record field <%s>", recordIdentifier));
			}
			if (StringUtils.hasText(recordValue)) {
				record.put(recordIdentifier, recordValue);
				lastEntry = recordIdentifier;
			}
		} while (true);
	}
}
