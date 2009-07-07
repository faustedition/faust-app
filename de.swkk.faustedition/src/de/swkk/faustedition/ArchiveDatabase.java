package de.swkk.faustedition;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.model.xmldb.Collection;
import de.faustedition.model.xmldb.ExistException;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.ResourceUtil;
import de.faustedition.util.XMLUtil;

@Component
public class ArchiveDatabase extends LinkedList<ArchiveRecord> implements InitializingBean {

	private static final long serialVersionUID = -7905171553768635446L;

	@Autowired
	private TranscriptionStore transcriptionStore;

	private Resource databaseResource = new ClassPathResource("/weimar_manuscripts_archive_db.xml");

	private Resource[] facsimileDirectoryCandidates = new FileSystemResource[] {
			new FileSystemResource("/home/faust/digitalisate"),
			new FileSystemResource("/Users/gregor/Desktop/Faustedition/data/facsimiles") };

	private File facsimileDirectory;

	private Map<GSACallNumber, ArchiveRecord> callNumberIndex = new HashMap<GSACallNumber, ArchiveRecord>();

	public void afterPropertiesSet() throws Exception {
		parse(this.databaseResource);

		Resource facsimileDirectoryResource = ResourceUtil.chooseExistingResources(facsimileDirectoryCandidates);
		if (facsimileDirectoryResource != null) {
			File facsimileDirectoryFile = facsimileDirectoryResource.getFile();
			LoggingUtil.info("Chose " + facsimileDirectoryResource.getDescription());
			if (facsimileDirectoryFile.isDirectory()) {
				facsimileDirectory = facsimileDirectoryFile;
			}
		}
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

	public void addFacsimiles(GSACallNumber callNumber, Collection folder) throws IOException, SAXException, TransformerException,
			ExistException {
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

		File[] facsimileFiles = facsimileBase.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return "jpg".equalsIgnoreCase(FilenameUtils.getExtension(name));
			}

		});

		for (File facsimileFile : facsimileFiles) {
			String transcriptionName = FilenameUtils.getBaseName(facsimileFile.getName());

			ManuscriptIdentifier msIdentifier = new ManuscriptIdentifier();
			msIdentifier.setInstitution("Klassik-Stiftung Weimar");
			msIdentifier.setRepository("Goethe- und Schiller-Archiv");
			msIdentifier.setCollection("Werke");
			msIdentifier.getIdentifiers().put("archive-db", Integer.toString(record.getIdentNum()));
			transcriptionStore.createTranscription(folder, transcriptionName, msIdentifier);
		}
	}
}
