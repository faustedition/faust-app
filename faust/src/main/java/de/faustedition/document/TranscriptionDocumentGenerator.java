package de.faustedition.document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.faustedition.Log;
import de.faustedition.facsimile.Facsimile;
import de.faustedition.facsimile.FacsimileStore;
import de.faustedition.report.Report;
import de.faustedition.report.ReportManager;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.tei.EncodedTextDocumentBuilder;
import de.faustedition.xml.XmlStore;

@Service
public class TranscriptionDocumentGenerator implements Runnable {
	@Autowired
	private FacsimileStore facsimileStore;

	@Autowired
	private XmlStore xmlStore;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;

	@Autowired
	private ReportManager reportManager;

	@Override
	public void run() {
		Log.LOGGER.info("Generating transcription documents");
		try {
			Set<URI> xml = Sets.newHashSet(xmlStore);
			Set<Facsimile> facsimiles = Sets.newHashSet(facsimileStore);

			Log.LOGGER.debug("Generating missing page-base transcription documents");
			for (Facsimile facsimile : facsimiles) {
				URI uri = XmlStore.WITNESS_BASE.resolve(new URI(null, facsimile.getPath() + ".xml", null));
				if (!xml.contains(uri)) {
					EncodedTextDocument document = documentBuilder.create();
					Facsimile.writeTo(document, Collections.singletonList(facsimile));

					Log.LOGGER.debug("Creating new page transcription document for facsimile '{}'", uri.toString());
					xmlStore.put(uri, document.getDom());
					xml.add(uri);
				}
			}

			Log.LOGGER.debug("Generating missing text-oriented transcription documents");
			final String witnessBasePath = XmlStore.WITNESS_BASE.getPath();
			for (URI uri : xml) {
				final String path = uri.getPath();
				if (!path.endsWith("/") || !path.startsWith(witnessBasePath)) {
					continue;
				}
				if (Iterables.any(xml, new IsXmlDocumentInCollectionPredicate(path))) {
					URI text = uri.resolve(new URI(null, FilenameUtils.getName(path.substring(0, path.length() - 1)) + ".xml", null));
					if (!xml.contains(text)) {
						Log.LOGGER.debug("Creating new text transcription document: '{}'", text.toString());
						xmlStore.put(text, documentBuilder.create().getDom());
					}
				}
			}

			Log.LOGGER.debug("Generating report on detached transcription documents");
			final SortedSet<URI> detached = Sets.newTreeSet();
			for (URI uri : xml) {
				String path = uri.getPath();
				if (!path.endsWith(".xml") || !path.startsWith(witnessBasePath)) {
					continue;
				}
				for (Facsimile facsimile : Facsimile.readFrom(new EncodedTextDocument(xmlStore.get(uri)))) {
					if (!facsimiles.contains(facsimile)) {
						detached.add(uri);
						break;
					}
				}
			}

			Report report = new Report("detached_transcription_documents");
			report.setBody(StringUtils.join(detached, "\n"));
			reportManager.send(report);
		} catch (IOException e) {
			Log.fatalError(e, "I/O error while generating page transcription documents");
		} catch (URISyntaxException e) {
			Log.fatalError(e, "URI encoding error while generating page transcription documents");
		}
	}

	private static class IsXmlDocumentInCollectionPredicate implements Predicate<URI> {

		private final String path;

		public IsXmlDocumentInCollectionPredicate(String path) {
			this.path = path;
		}

		@Override
		public boolean apply(URI input) {
			String inputPath = input.getPath();
			if (!inputPath.startsWith(path)) {
				return false;
			}
			inputPath = inputPath.substring(path.length());
			return inputPath.endsWith(".xml") && (inputPath.indexOf('/') < 0);
		}
	}
}
