package de.swkk.metadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.xml.XmlUtil;

public class ArchiveDatabase extends LinkedList<ArchiveDatabaseRecord> {
	private static final Resource DATABASE_RESOURCE = new ClassPathResource("archive-database.xml", ArchiveDatabase.class);

	private Map<GSACallNumber, ArchiveDatabaseRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveDatabaseRecord>();

	public static ArchiveDatabase parse() throws SAXException, IOException {
		final ArchiveDatabase db = new ArchiveDatabase();
		XmlUtil.saxParser().parse(DATABASE_RESOURCE.getInputStream(), new DefaultHandler() {

			private ArchiveDatabaseRecord record;
			private String currentProperty;
			private StringBuilder currentValue;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
					db.add(record);
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
		return db;
	}
	

	private ArchiveDatabase() {
	}
	
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
	public boolean add(ArchiveDatabaseRecord o) {
		if (callNumberIndex.containsKey(o.getCallNumber())) {
			throw new IllegalStateException(o.getCallNumber().toString());
		}
		super.add(o);
		callNumberIndex.put(o.getCallNumber(), o);
		return true;
	}
}
