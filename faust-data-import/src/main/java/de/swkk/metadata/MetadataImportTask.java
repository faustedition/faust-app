package de.swkk.metadata;

import static de.faustedition.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.faustedition.ErrorUtil;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.tei.EncodedTextDocumentManager;
import de.faustedition.xml.XmlStore;
import de.faustedition.xml.XmlUtil;

@Service
public class MetadataImportTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MetadataImportTask.class);
	private static final Pattern SIGLA_PATTERN = Pattern.compile("[A-Za-z0-9]");

	@Autowired
	private XmlStore xmlDbManager;

	@Autowired
	private EncodedTextDocumentManager documentManager;

	private MetadataFieldMapping mapping = new MetadataFieldMapping();

	public void run() {
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
		InventoryDatabase inventoryDb = InventoryDatabase.parse();
		ArchiveDatabase archiveDb = ArchiveDatabase.parse();

		for (AllegroRecord allegroRecord : inventoryDb) {
			GSACallNumber callNumber = inventoryDb.getCallNumber(allegroRecord);
			MetadataRecord metadata = mapping.map(allegroRecord);
			metadata.remove("callnumber_old");
			for (ArchiveDatabaseRecord archiveDbRecord : archiveDb.filter(callNumber)) {
				String portfolioName = Integer.toString(archiveDbRecord.getIdentNum());

				URI portfolio = URI.create(String.format("Witness/GSA/%s/", portfolioName));
				LOG.info("Importing metadata for GSA signature '{}' to '{}'", callNumber, portfolio.toString());

				URI destinationUri = null;
				EncodedTextDocument destination = null;
				try {
					for (URI portfolioContent : xmlDbManager.list(portfolio)) {
						if (portfolio.relativize(portfolioContent).getPath().startsWith(portfolioName)) {
							destinationUri = portfolioContent;
							destination = new EncodedTextDocument(xmlDbManager.get(destinationUri));
							break;
						}
					}
				} catch (HttpClientErrorException e) {
					if (!HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
						throw e;
					}
				}

				if (destinationUri == null) {
					destinationUri = portfolio.resolve(portfolioName + ".xml");
					destination = documentManager.create();
				}

				documentManager.process(destination);
				LOG.info("Writing metadata to {}", destinationUri);
				writeMetadataTo(destination, metadata, portfolioName, archiveDbRecord.getCallNumber());
				documentManager.process(destination);
				xmlDbManager.put(destinationUri, destination.getDom());

				// MetadataRecord record =
				// MetadataRecord.fromXml(dom.getDocumentElement());
				// record.merge(newMetadata);
				// record.put("callnumber", portfolioName);
				//
				// XmlUtil.removeChildren(dom.getDocumentElement());
				// record.toXml(dom.getDocumentElement());
				//
				// xmlDbManager.put(portfolio, dom);
			}
		}
	}

	private void writeMetadataTo(EncodedTextDocument d, MetadataRecord m, String portfolioName, GSACallNumber callNumber) {
		Element sourceDesc = singleResult(xpath("//tei:teiHeader/tei:fileDesc/tei:sourceDesc"), d.getDom(), Element.class);
		Assert.notNull(sourceDesc, "No source description found in header");
		if (!XmlUtil.hasText(sourceDesc)) {
			XmlUtil.removeChildren(sourceDesc);
		}

		Element msDesc = findOrCreateChildElement(sourceDesc, "msDesc");
		Element msIdentifier = findOrCreateChildElement(msDesc, "msIdentifier");

		Element settlement = findOrCreateChildElement(msIdentifier, "settlement");
		if (StringUtils.isBlank(settlement.getTextContent())) {
			settlement.setTextContent("Weimar");
		}

		Element institution = findOrCreateChildElement(msIdentifier, "institution");
		if (StringUtils.isBlank(institution.getTextContent())) {
			institution.setTextContent("Klassik Stiftung");
		}

		Element repository = findOrCreateChildElement(msIdentifier, "repository");
		if (StringUtils.isBlank(repository.getTextContent())) {
			repository.setTextContent("Goethe- und Schiller-Archiv");
		}

		if (m.containsKey("portfolio")) {
			Element collection = singleResult(xpath("./tei:collection"), msIdentifier, Element.class);
			if (collection == null) {
				collection = d.getDom().createElementNS(TEI_NS_URI, "collection");
				msIdentifier.appendChild(collection);
			}
			if (StringUtils.isBlank(collection.getTextContent())) {
				collection.setTextContent(m.get("portfolio"));
			}
		}

		Element idNo = findOrCreateChildElement(msIdentifier, "idno");
		if (StringUtils.isBlank(idNo.getTextContent())) {
			idNo.setTextContent(portfolioName);
		}

		Element oldId = singleResult(xpath("./tei:altIdentifier[@type='repository']/tei:idno"), msIdentifier, Element.class);
		if (oldId == null) {
			Element idWrapper = d.getDom().createElementNS(TEI_NS_URI, "altIdentifier");
			idWrapper.setAttribute("type", "repository");
			msIdentifier.appendChild(idWrapper);
			oldId = d.getDom().createElementNS(TEI_NS_URI, "idno");
			idWrapper.appendChild(oldId);
		}
		if (StringUtils.isBlank(oldId.getTextContent())) {
			oldId.setTextContent(callNumber.toString());
		}

		if (m.containsKey("id_weimarer_ausgabe") && SIGLA_PATTERN.matcher(m.get("id_weimarer_ausgabe")).find()) {
			Element waId = singleResult(xpath("./tei:altIdentifier[@type='edition']/tei:idno"), msIdentifier,
					Element.class);
			if (waId == null) {
				Element idWrapper = d.getDom().createElementNS(TEI_NS_URI, "altIdentifier");
				idWrapper.setAttribute("type", "edition");
				msIdentifier.appendChild(idWrapper);
				waId = d.getDom().createElementNS(TEI_NS_URI, "idno");
				idWrapper.appendChild(waId);
			}
			if (StringUtils.isBlank(waId.getTextContent())) {
				waId.setTextContent(m.get("id_weimarer_ausgabe"));
			}
		}

		if (m.containsKey("manuscript_reference_weimarer_ausgabe")
				&& SIGLA_PATTERN.matcher(m.get("manuscript_reference_weimarer_ausgabe")).find()) {
			Element printId = singleResult(xpath("./tei:altIdentifier[@type='print']/tei:idno"), msIdentifier,
					Element.class);
			if (printId == null) {
				Element idWrapper = d.getDom().createElementNS(TEI_NS_URI, "altIdentifier");
				idWrapper.setAttribute("type", "print");
				msIdentifier.appendChild(idWrapper);
				printId = d.getDom().createElementNS(TEI_NS_URI, "idno");
				idWrapper.appendChild(printId);
			}
			if (StringUtils.isBlank(printId.getTextContent())) {
				printId.setTextContent(m.get("manuscript_reference_weimarer_ausgabe"));
			}
		}

		if (m.containsKey("work_genetic_level_goethe")) {
			Element historySummary = singleResult(xpath("./tei:history/tei:summary"), msDesc, Element.class);
			if (historySummary == null) {
				Element history = d.getDom().createElementNS(TEI_NS_URI, "history");
				msDesc.insertBefore(history, msIdentifier.getNextSibling());
				historySummary = d.getDom().createElementNS(TEI_NS_URI, "summary");
				history.appendChild(historySummary);
			}
			if (StringUtils.isBlank(historySummary.getTextContent())) {
				historySummary.setTextContent(m.get("work_genetic_level_goethe"));
			}
		}
	}

	private Element findOrCreateChildElement(Element parent, String name) {
		Element child = singleResult(xpath("./tei:" + name), parent, Element.class);
		if (child == null) {
			child = parent.getOwnerDocument().createElementNS(TEI_NS_URI, name);
			parent.appendChild(child);
		}
		return child;
	}
}
