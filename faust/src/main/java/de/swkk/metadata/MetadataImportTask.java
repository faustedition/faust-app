package de.swkk.metadata;

import static de.faustedition.model.xml.NodeListIterable.singleResult;
import static de.faustedition.model.xml.XmlDocument.FAUST_NS_URI;
import static de.faustedition.model.xml.XmlDocument.xpath;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.faustedition.ErrorUtil;
import de.faustedition.model.metadata.MetadataRecord;
import de.faustedition.model.xml.XmlDbManager;
import de.faustedition.model.xml.XmlDocument;
import de.faustedition.model.xml.XmlUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;
import de.swkk.metadata.inventory.FaustInventory;
import de.swkk.metadata.inventory.MetadataFieldMapping;

@Service
public class MetadataImportTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MetadataImportTask.class);

	@Autowired
	private XmlDbManager xmlDbManager;

	private MetadataFieldMapping mapping = new MetadataFieldMapping();

	@Override
	public void run() {
		if (singleResult(xpath("//f:resource[contains(text(), 'metadata.xml')]"), xmlDbManager.resources(), Element.class) != null) {
			LOG.debug("XML database contains metadata; skipping import");
			return;
		}

		try {
			LOG.info("Importing metadata ...");
			StopWatch sw = new StopWatch();
			sw.start();
			doImport();
			sw.stop();
			LOG.info("Metadata imported in " + sw);
		} catch (Exception e) {
			ErrorUtil.fatal(e, "Error while importing metadata");
		}
	}

	public void doImport() throws IOException, SAXException {
		FaustInventory faustInventory = FaustInventory.parse();
		ArchiveDatabase archiveDatabase = ArchiveDatabase.parse();

		for (AllegroRecord allegroRecord : faustInventory) {
			GSACallNumber callNumber = faustInventory.getCallNumber(allegroRecord);
			MetadataRecord newMetadata = mapping.map(allegroRecord);
			newMetadata.remove("callnumber_old");
			for (ArchiveDatabaseRecord archiveDbRecord : archiveDatabase.filter(callNumber)) {
				newMetadata.put("callnumber_old", archiveDbRecord.getCallNumber().toString());
				String identNum = Integer.toString(archiveDbRecord.getIdentNum());

				URI metadataUri = URI.create(String.format("Witness/GSA/%s/metadata.xml", identNum));
				LOG.debug("Importing metadata for GSA signature '{}' to '{}'", callNumber, metadataUri.toString());

				Document dom = null;
				try {
					dom = (Document) xmlDbManager.get(metadataUri);
				} catch (HttpClientErrorException e) {
					if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
						dom = new XmlDocument().getDom();
						dom.appendChild(dom.createElementNS(FAUST_NS_URI, "metadata"));
					} else {
						throw e;
					}
				}

				MetadataRecord record = MetadataRecord.fromXml(dom.getDocumentElement());
				record.merge(newMetadata);
				record.put("callnumber", identNum);

				XmlUtil.removeChildren(dom.getDocumentElement());
				record.toXml(dom.getDocumentElement());

				xmlDbManager.put(metadataUri, dom);
			}
		}
	}
}
