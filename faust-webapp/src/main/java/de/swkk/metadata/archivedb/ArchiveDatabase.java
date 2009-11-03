package de.swkk.metadata.archivedb;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.util.ErrorUtil;
import de.swkk.metadata.GSACallNumber;

public class ArchiveDatabase extends LinkedList<ArchiveDatabaseRecord> {
	private static final Resource DATABASE_RESOURCE = new ClassPathResource("/weimar_manuscripts_archive_db.xml");

	private Map<GSACallNumber, ArchiveDatabaseRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveDatabaseRecord>();

	public ArchiveDatabase() throws SAXException, IOException {
		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			throw ErrorUtil.fatal(e, "Error configuring SAX parser");
		}
		
		parser.parse(DATABASE_RESOURCE.getInputStream(), new DefaultHandler() {

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
					add(record);
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
