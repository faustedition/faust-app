package de.faustedition.model.facsimile;

import static de.faustedition.model.xml.XmlDocument.xpath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.tei.EncodedTextDocumentManager;
import de.faustedition.model.xml.NodeListIterable;
import de.faustedition.model.xml.XmlDbManager;

@Service
public class FacsimileUpdateTask {
	private static final Logger LOG = LoggerFactory.getLogger(FacsimileUpdateTask.class);
	private static final URI FACSIMILES_URI = URI.create("Facsimiles/");
	@Autowired
	private FacsimileManager manager;

	@Autowired
	private XmlDbManager xmlDbManager;

	@Autowired
	private EncodedTextDocumentManager documentManager;

	public void update() {
		LOG.info("Updating facsimiles ...");
		StopWatch sw = new StopWatch();
		sw.start();

		manager.generateAll();
		final SortedSet<String> facsimilePaths = manager.findAllPaths();
		Document facsimileReferences = xmlDbManager.facsimileReferences();
		for (Element facsimileReference : new NodeListIterable<Element>(xpath("//f:ref"), facsimileReferences)) {
			String uri = facsimileReference.getTextContent();
			try {
				facsimilePaths.remove(FacsimileReference.fromURI(new URI(uri)).getPath());
			} catch (URISyntaxException e) {
				LOG.warn("Invalid facsimile reference URI: {}", uri);
			}
		}
		for (String facsimilePath : facsimilePaths) {
			EncodedTextDocument document = documentManager.create();
			FacsimileReference.writeTo(document, Collections.singletonList(new FacsimileReference(facsimilePath)));			
			URI templateDocUri = FACSIMILES_URI.resolve(facsimilePath.replaceAll("/", "_") + ".xml");
			LOG.debug("Creating new template document for facsimile '{}'", templateDocUri.toString());
			xmlDbManager.put(templateDocUri, document.getDom());
		}
		
		sw.stop();
		LOG.info("Updated facsimiles in {}", sw);
	}
}
