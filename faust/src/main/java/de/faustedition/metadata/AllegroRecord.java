package de.faustedition.metadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

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

	public static class Set extends TreeSet<AllegroRecord> implements InitializingBean {
		private static final long serialVersionUID = -4027794104909308384L;

		private Resource source;

		public void setSource(Resource source) {
			this.source = source;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			BufferedReader recordSetReader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
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

				String recordIdentifier = StringUtils.trimWhitespace(recordLine.substring(1,
						Math.min(recordLine.length(), 4)));
				String recordValue = StringUtils.trimWhitespace(recordLine.substring(Math.min(recordLine.length(),
						4)));

				if (record.containsKey(recordIdentifier)) {
					throw new IllegalStateException(String.format("Duplicate record field <%s>",
							recordIdentifier));
				}
				if (StringUtils.hasText(recordValue)) {
					record.put(recordIdentifier, recordValue);
					lastEntry = recordIdentifier;
				}
			} while (true);
		}

	}
}
