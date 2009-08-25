package de.swkk.metadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

public class ArchiveDatabase extends LinkedList<ArchiveRecord> implements InitializingBean {

	private Resource databaseResource;

	private Map<GSACallNumber, ArchiveRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveRecord>();

	@Required
	public void setDatabaseResource(Resource databaseResource) {
		this.databaseResource = databaseResource;
	}

	public void afterPropertiesSet() throws Exception {
		parse(this.databaseResource);
	}

	public ArchiveRecord lookup(GSACallNumber callNumber) {
		StringBuilder lookupStr = new StringBuilder();
		lookupStr.append(callNumber.getPortfolio() == null ? "25" : callNumber.getPortfolio().toString());
		lookupStr.append("/");
		lookupStr.append(callNumber.getSubPortfolio());
		lookupStr.append(",");
		lookupStr.append(callNumber.getFile());
		if (callNumber.getSubFile() != null) {
			lookupStr.append(",");
			lookupStr.append(callNumber.getSubFile());
		}

		LoggingUtil.LOG.info(String.format("Looking up GSA call number [%s]", lookupStr.toString()));
		return callNumberIndex.get(new GSACallNumber(lookupStr.toString()));
	}

	protected void parse(Resource resource) throws SAXException, IOException {
		clear();
		XMLUtil.parse(resource.getInputStream(), new DefaultHandler() {

			private ArchiveRecord record;
			private String currentProperty;
			private StringBuilder currentValue;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if ("ITEM".equals(qName)) {
					record = new ArchiveRecord();

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

	@Override
	public boolean add(ArchiveRecord o) {
		if (callNumberIndex.containsKey(o.getCallNumber())) {
			throw new IllegalStateException(o.getCallNumber().toString());
		}
		super.add(o);
		callNumberIndex.put(o.getCallNumber(), o);
		return true;
	}
}
