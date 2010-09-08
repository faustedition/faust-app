package de.faustedition.metadata;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.xml.XmlUtil;

public class ArchiveDatabaseRecord extends LinkedHashMap<String, String> implements Comparable<ArchiveDatabaseRecord> {

	private static final long serialVersionUID = 860569636214390541L;

	public GSACallNumber getCallNumber() {
		if (containsKey("bestandnr") && containsKey("signatur")) {
			return new GSACallNumber(String.format("%s/%s", get("bestandnr"), get("signatur")));
		}

		return null;
	}

	public int getId() {
		return Integer.parseInt(get("num"));
	}

	public Integer getIdentNum() {
		Assert.isTrue(containsKey("ident"));
		return Integer.parseInt(get("ident"));
	}

	public void dump(PrintStream printStream) {
		for (Map.Entry<String, String> field : this.entrySet()) {
			printStream.printf("%s: %s\n", field.getKey(), field.getValue());
		}
	}

	public int compareTo(ArchiveDatabaseRecord o) {
		return getCallNumber().compareTo(o.getCallNumber());
	}

	@Service
	public static class List extends LinkedList<ArchiveDatabaseRecord> implements InitializingBean {

		private static final long serialVersionUID = 8261096570226360946L;
		private Map<GSACallNumber, ArchiveDatabaseRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveDatabaseRecord>();

		public SortedSet<ArchiveDatabaseRecord> collect(GSACallNumber callNumber) {
			SortedSet<ArchiveDatabaseRecord> records = new TreeSet<ArchiveDatabaseRecord>();
			for (GSACallNumber dbCallNumber : callNumberIndex.keySet()) {
				if (callNumber.contains(dbCallNumber)) {
					records.add(callNumberIndex.get(dbCallNumber));
				}
			}
			return records;
		}

		public SortedSet<ArchiveDatabaseRecord> filter(GSACallNumber callNumber) {
			SortedSet<ArchiveDatabaseRecord> records = new TreeSet<ArchiveDatabaseRecord>();
			for (GSACallNumber dbCallNumber : callNumberIndex.keySet()) {
				if (dbCallNumber.contains(callNumber)) {
					records.add(callNumberIndex.get(dbCallNumber));
				}
			}
			return records;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			final ClassPathResource source = new ClassPathResource("gsa-archive-database.xml",
					ArchiveDatabaseRecord.class);
			InputStream sourceStream = null;
			try {
				XmlUtil.saxParser().parse(sourceStream = source.getInputStream(), new DefaultHandler() {

					private ArchiveDatabaseRecord record;
					private String currentProperty;
					private StringBuilder currentValue;

					@Override
					public void startElement(String uri, String localName, String qName, Attributes attributes)
							throws SAXException {
						if ("ITEM".equals(qName)) {
							record = new ArchiveDatabaseRecord();

							int numAttrIndex = attributes.getIndex("num");
							if (numAttrIndex >= 0) {
								record.put("num", attributes.getValue(numAttrIndex));
							}
						} else if (record != null) {
							currentProperty = qName.toLowerCase();
							currentValue = new StringBuilder();
						}
					}

					@Override
					public void endElement(String uri, String localName, String qName) throws SAXException {
						if ("ITEM".equals(qName)) {
							if (callNumberIndex.containsKey(record.getCallNumber())) {
								throw new IllegalStateException(record.getCallNumber().toString());
							}
							add(record);
							callNumberIndex.put(record.getCallNumber(), record);
							record = null;
						} else if (currentProperty != null) {
							record.put(currentProperty, currentValue.toString());
							currentProperty = null;
							currentValue = null;
						}

					}

					@Override
					public void characters(char[] ch, int start, int length) throws SAXException {
						if (currentValue != null) {
							currentValue.append(ch, start, length);
						}
					}
				});
			} finally {
				IOUtils.closeQuietly(sourceStream);
			}
		}

	}
}
