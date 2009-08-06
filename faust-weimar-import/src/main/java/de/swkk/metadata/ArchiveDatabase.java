package de.swkk.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.model.Folder;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.ResourceUtil;
import de.faustedition.util.XMLUtil;

public class ArchiveDatabase extends LinkedList<ArchiveRecord> implements InitializingBean {

	private Resource databaseResource;

	private Resource[] facsimileDirectoryCandidates;

	private File facsimileDirectory;

	private Map<GSACallNumber, ArchiveRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveRecord>();

	@Required
	public void setDatabaseResource(Resource databaseResource) {
		this.databaseResource = databaseResource;
	}

	@Required
	public void setFacsimileDirectoryCandidates(Resource[] facsimileDirectoryCandidates) {
		this.facsimileDirectoryCandidates = facsimileDirectoryCandidates;
	}

	public void afterPropertiesSet() throws Exception {
		parse(this.databaseResource);

		Resource facsimileDirectoryResource = ResourceUtil.chooseExistingResources(facsimileDirectoryCandidates);
		facsimileDirectory = (facsimileDirectoryResource == null || facsimileDirectoryResource.getFile().isDirectory() ? facsimileDirectoryResource.getFile() : null);
		Assert.notNull(facsimileDirectory);
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

		LoggingUtil.log(Level.INFO, String.format("Looking up GSA call number [%s]", lookupStr.toString()));
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

	public void addFacsimiles(GSACallNumber callNumber, Folder folder) throws IOException, DocumentException {
		if (callNumber.isContent()) {
			return;
		}

		ArchiveRecord record = lookup(callNumber);
		if (record == null) {
			return;
		}

		File facsimileBase = new File(facsimileDirectory, Integer.toString(record.getIdentNum()));
		if (!facsimileBase.isDirectory()) {
			return;
		}

		LoggingUtil.log(Level.INFO, String.format("Searching for facsimiles in [%s]", facsimileBase.getAbsolutePath()));
		File[] facsimileFiles = facsimileBase.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return "jpg".equalsIgnoreCase(FilenameUtils.getExtension(name));
			}

		});

		for (File facsimileFile : facsimileFiles) {
			LoggingUtil.log(Level.INFO, String.format("Creating facsimile [%s] in [%s]", facsimileFile.getName(), folder.getFile().getAbsolutePath()));
			//transcriptionManager.createTranscription(folder, facsimileFile);
		}
	}
}
