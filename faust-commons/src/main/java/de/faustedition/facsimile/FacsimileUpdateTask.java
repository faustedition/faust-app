package de.faustedition.facsimile;

import static de.faustedition.xml.XmlDocument.xpath;

import java.io.IOException;
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

import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.tei.EncodedTextDocumentBuilder;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlStore;

@Service
public class FacsimileUpdateTask {
	private static final Logger LOG = LoggerFactory.getLogger(FacsimileUpdateTask.class);
	private static final URI FACSIMILES_URI = URI.create("Facsimiles/");

	@Autowired
	private FacsimileTileStore store;

	@Autowired
	private XmlStore xmlStore;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;

	public void update() throws IOException {
		LOG.info("Updating facsimiles ...");
		StopWatch sw = new StopWatch();
		sw.start();

		store.build();
		final SortedSet<Facsimile> facsimiles = store.all();
		Document facsimileReferences = xmlStore.facsimileReferences();
		for (Element facsimileReference : new NodeListIterable<Element>(xpath("//f:ref"), facsimileReferences)) {
			String uri = facsimileReference.getTextContent();
			try {
				facsimiles.remove(Facsimile.fromUri(new URI(uri)));
			} catch (URISyntaxException e) {
				LOG.warn("Invalid facsimile reference URI: {}", uri);
			}
		}
		for (Facsimile facsimile : facsimiles) {
			EncodedTextDocument document = documentBuilder.create();
			Facsimile.writeTo(document, Collections.singletonList(facsimile));
			URI templateDocUri = FACSIMILES_URI.resolve(facsimile.getPath().replaceAll("/", "_") + ".xml");
			LOG.debug("Creating new template document for facsimile '{}'", templateDocUri.toString());
			xmlStore.put(templateDocUri, document.getDom());
		}

		sw.stop();
		LOG.info("Updated facsimiles in {}", sw);
	}
}
