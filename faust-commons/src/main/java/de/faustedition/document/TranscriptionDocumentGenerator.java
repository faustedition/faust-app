package de.faustedition.document;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.faustedition.Log;
import de.faustedition.facsimile.Facsimile;
import de.faustedition.facsimile.FacsimileStore;
import de.faustedition.report.Report;
import de.faustedition.report.ReportSender;
import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.tei.EncodedTextDocumentBuilder;
import de.faustedition.xml.XmlStore;

@Service
public class TranscriptionDocumentGenerator implements Runnable {
	private static final URI WITNESS_BASE = URI.create("Witness/");

	@Autowired
	private FacsimileStore facsimileStore;

	@Autowired
	private XmlStore xmlStore;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;

	@Autowired
	private ReportSender reportSender;

	@Override
	public void run() {
		Log.LOGGER.info("Generating transcription documents");
		try {
			Set<URI> xml = Sets.newHashSet(xmlStore);
			Set<Facsimile> facsimiles = Sets.newHashSet(facsimileStore);

			Log.LOGGER.debug("Generating missing page-base transcription documents");
			for (Facsimile facsimile : facsimiles) {
				URI uri = WITNESS_BASE.resolve(facsimile.getPath() + ".xml");
				if (!xml.contains(uri)) {
					EncodedTextDocument document = documentBuilder.create();
					Facsimile.writeTo(document, Collections.singletonList(facsimile));

					Log.LOGGER.debug("Creating new page transcription document for facsimile '{}'", uri.toString());
					xmlStore.put(uri, document.getDom());
					xml.add(uri);
				}
			}

			Log.LOGGER.debug("Generating missing text-oriented transcription documents");
			final String witnessBasePath = WITNESS_BASE.getPath();
			for (URI uri : xml) {
				final String path = uri.getPath();
				if (!path.endsWith("/") || !path.startsWith(witnessBasePath)) {
					continue;
				}
				if (Iterables.any(xml, new IsXmlDocumentInCollectionPredicate(path))) {
					URI text = uri.resolve(FilenameUtils.getName(path.substring(0, path.length() - 1)) + ".xml");
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
				path = path.substring(witnessBasePath.length(), path.length() - 4);

				String name = FilenameUtils.getName(path);
				if (FilenameUtils.getPathNoEndSeparator(path).endsWith(name)) {
					// text-oriented transcription
					continue;
				}

				if (!facsimiles.contains(new Facsimile(path))) {
					detached.add(uri);
				}
			}
			reportSender.send(new Report() {

				@Override
				public void printBody(PrintWriter body) {
					for (URI uri : detached) {
						body.println(uri.toString());
					}
				}

				@Override
				public boolean isEmpty() {
					return detached.isEmpty();
				}

				@Override
				public String getSubject() {
					return "Detached transcription documents";
				}
			});
		} catch (IOException e) {
			Log.fatalError(e, "I/O error while generating page transcription documents");
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
