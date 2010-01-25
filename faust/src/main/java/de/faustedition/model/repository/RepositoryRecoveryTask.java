package de.faustedition.model.repository;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;
import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import de.faustedition.model.document.ArchiveReference;
import de.faustedition.model.document.DocumentDating;
import de.faustedition.model.document.LegacyMetadata;
import de.faustedition.model.document.PrintReference;
import de.faustedition.model.document.TranscriptionStatus;
import de.faustedition.model.document.TranscriptionStatusMixin;
import de.faustedition.model.facsimile.Facsimile;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.tei.EncodedTextDocumentManager;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.URIUtil;
import de.faustedition.util.XMLUtil;

@Service
public class RepositoryRecoveryTask implements Runnable {
	@Autowired
	private EncodedTextDocumentManager encodedDocumentManager;

	@Autowired
	@Qualifier("backup")
	private File backupDirectory;

	@Autowired
	private javax.jcr.Repository repository;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@PostConstruct
	public void scheduleRecovery() {
		Executors.newSingleThreadExecutor().execute(this);
	}

	public void run() {
		javax.jcr.Session repoSession = null;
		try {
			RepositoryFolder appNode = RepositoryUtil.appNode(repoSession = RepositoryUtil.login(repository));
			if (appNode.getNode().hasNodes()) {
				LoggingUtil.LOG.debug("Repository is not empty; skip recovery");
				return;
			}
			
			StopWatch sw = new StopWatch();
			sw.start();
			
			if (recoverFromBackup(repoSession)) {
				sw.stop();
				LoggingUtil.LOG.info("Repository recovered from backup in " + sw);
				return;
			}

			migrateFromRdbms(repoSession);
			sw.stop();
			LoggingUtil.LOG.info("Repository recovered from RDBMS in " + sw);
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal(e, "Repository error while recovering repository: %s", e.getMessage());
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while recovering repository: %s", e.getMessage());
		} finally {
			RepositoryUtil.logoutQuietly(repoSession);
		}

	}

	private boolean recoverFromBackup(javax.jcr.Session repoSession) throws IOException, RepositoryException {
		List<File> backupFiles = Arrays.asList(backupDirectory.listFiles(RepositoryBackupTask.BACKUP_FILE_FILTER));
		if (backupFiles.isEmpty()) {
			return false;
		}

		Collections.sort(backupFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return (-1) * o1.getName().compareTo(o2.getName());
			}
		});

		File backupFile = backupFiles.get(0);
		LoggingUtil.LOG.debug("Recovering repository from '" + backupFile.getAbsolutePath() + "'");

		InputStream backupStream = null;
		try {
			backupStream = new GZIPInputStream(new FileInputStream(backupFile));

			RepositoryUtil.appNode(repoSession).getNode().remove();
			repoSession.save();
			repoSession.getWorkspace().importXML("/", backupStream, IMPORT_UUID_COLLISION_THROW);
			return true;
		} finally {
			IOUtils.closeQuietly(backupStream);
		}
	}

	private void migrateFromRdbms(final javax.jcr.Session repoSession) {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					RepositoryFolder appNode = RepositoryUtil.appNode(repoSession);
					Session session = sessionFactory.getCurrentSession();

					LoggingUtil.LOG.info("Repository recovery; migrating data from RDBMS");
					RepositoryFolder documents = RepositoryFolder.create(appNode, "Dokumente");
					for (Repository repository : Repository.find(session)) {
						RepositoryFolder repoFolder = RepositoryFolder.create(documents, repository
								.getName());
						LoggingUtil.LOG.debug("Migrating " + repoFolder);
						migrateRepository(session, repository, repoFolder);
					}

					LoggingUtil.LOG.debug("Creating text node");
					RepositoryFolder.create(appNode, "Texte");
					appNode.save();
				} catch (RepositoryException e) {
					throw ErrorUtil.fatal(e, "Repository error while recovering from RDBMS");
				}
			}
		});
	}

	private void migrateRepository(Session session, Repository repository, RepositoryFolder folder) throws RepositoryException {
		for (Portfolio portfolio : Portfolio.find(session, repository)) {
			RepositoryFolder portfolioFolder = RepositoryFolder.create(folder, portfolio.getName());
			LoggingUtil.LOG.debug("Migrating " + portfolioFolder);
			// for (HierarchyNodeFacet facet :
			// buildPortfolioFacets(session, repository, portfolio))
			// {
			// facet.setFacettedNode(pn);
			// facet.save(session);
			// }
			migratePortfolio(session, portfolio, portfolioFolder);
			session.flush();
			session.clear();
		}

	}

	private void migratePortfolio(Session session, Portfolio portfolio, RepositoryFolder folder) throws RepositoryException {
		for (Manuscript manuscript : Manuscript.find(session, portfolio)) {
			String name = manuscript.getName();
			RepositoryFolder pageFolder = RepositoryFolder.create(folder, name);

			Facsimile facsimile = Facsimile.find(session, manuscript, name);
			if (facsimile != null) {
				Transcription transcription = Transcription.find(session, facsimile);
				if (transcription != null) {
					EncodedTextDocument document = transcription.buildTEIDocument(encodedDocumentManager);
					Document xml = document.getDocument();
					Element teiEl = xml.getDocumentElement();
					Element textEl = document.findNode("/:TEI/:text", Element.class);

					Element facsimileRef = xml.createElementNS(TEI_NS_URI, "graphic");
					URI facsimileUri = URIUtil.createFacsimileURI(facsimile.getImagePath());
					facsimileRef.setAttributeNS(TEI_NS_URI, "url", facsimileUri.toASCIIString());
					
					Element facsimileEl = xml.createElementNS(TEI_NS_URI, "facsimile");
					facsimileEl.appendChild(facsimileRef);					
					if (textEl != null) {
						teiEl.insertBefore(facsimileEl, textEl);
					} else {
						teiEl.appendChild(facsimileEl);
					}
					
					TranscriptionStatus status = TranscriptionStatus.extract(document);
					if (TranscriptionStatus.EMPTY.equals(status)) {
						if (textEl != null && XMLUtil.hasText(textEl)) {
							Element teiHeader = document.findNode("/:TEI/:teiHeader", Element.class);
							Element revisionDesc = EncodedTextDocument.findNode(teiHeader,
									"./:revisionDesc", Element.class);

							if (revisionDesc == null) {
								revisionDesc = xml.createElementNS(TEI_NS_URI,
										"revisionDesc");
								teiHeader.appendChild(revisionDesc);
							}
							
							Element change = xml.createElementNS(TEI_NS_URI, "change");
							revisionDesc.appendChild(change);

							String today = DateFormatUtils.ISO_DATE_FORMAT.format(System
									.currentTimeMillis());
							change.setAttributeNS(TEI_NS_URI, "when", today);
							change.setAttributeNS(TEI_NS_URI, "who", "system");
							if (Pattern.matches("^0*1$", name)) {
								change.setTextContent("Rohzustand");
								status = TranscriptionStatus.RAW;
							} else {
								change.setTextContent("kodiert");
								status = TranscriptionStatus.ENCODED;
							}
						}
					}
					RepositoryDocument file = RepositoryDocument.create(pageFolder, name + ".xml", document);
					TranscriptionStatusMixin.create(file, status);

					session.flush();
					session.clear();
					folder.save();
					LoggingUtil.LOG.debug("Migrated " + file);
				}
			}

		}
	}

	private Collection<HierarchyNodeFacet> buildPortfolioFacets(Session session, Repository repository, Portfolio portfolio) {
		ArchiveReference archiveFacet = new ArchiveReference();
		archiveFacet.setRepository(repository.getName());
		archiveFacet.setCallnumber(portfolio.getName());

		PrintReference printReferenceFacet = null;
		LegacyMetadata legacyMetadataFacet = null;
		DocumentDating datingFacet = null;

		for (MetadataAssignment ma : MetadataAssignment.find(session, Portfolio.class.getName(), portfolio.getId())) {
			String field = ma.getField();
			if ("callnumber_old".equals(field)) {
				String[] callnumbers = StringUtils.split(ma.getValue());
				for (int cc = 0; cc < callnumbers.length; cc++) {
					callnumbers[cc] = StringUtils.substringBeforeLast(callnumbers[cc].trim(), "*");
				}

				archiveFacet.setLegacyCallnumber("");
				for (String cn : callnumbers) {
					if (!archiveFacet.getLegacyCallnumber().contains(cn)) {
						archiveFacet.setLegacyCallnumber(archiveFacet.getLegacyCallnumber() + " " + cn);
					}
				}
				archiveFacet.setLegacyCallnumber(StringUtils.trimToNull(archiveFacet.getLegacyCallnumber()));
			} else if ("id_weimarer_ausgabe".equals(field) || "print_weimarer_ausgabe".equals(field)
					|| "print_weimarer_ausgabe_additional".equals(field)) {
				String id = normalizeWhitespace(StringUtils.remove(StringUtils.remove("oS", ma.getValue()), "-"))
						.trim();
				if (id.length() > 0) {
					if (printReferenceFacet == null) {
						printReferenceFacet = new PrintReference();
					}
					if (printReferenceFacet.getReferenceWeimarerAusgabe() == null) {
						printReferenceFacet.setReferenceWeimarerAusgabe("");
					}
					printReferenceFacet.setReferenceWeimarerAusgabe(StringUtils.trimToNull(printReferenceFacet
							.getReferenceWeimarerAusgabe()
							+ " " + id));
				}
			} else if ("manuscript_reference_weimarer_ausgabe".equals(field)) {
				if (printReferenceFacet == null) {
					printReferenceFacet = new PrintReference();
				}
				printReferenceFacet.setManuscriptReferenceWeimarerAusgabe(normalizeWhitespace(ma.getValue()));
			} else if ("id_paralipomenon_weimarer_ausgabe".equals(field)) {
				if (printReferenceFacet == null) {
					printReferenceFacet = new PrintReference();
				}
				printReferenceFacet.setParalipomenonReferenceWeimarerAusgabe(normalizeWhitespace(ma.getValue()));
			} else if ("record_number".equals(field)) {
				if (legacyMetadataFacet == null) {
					legacyMetadataFacet = new LegacyMetadata();
				}
				legacyMetadataFacet.setRecordNumber(normalizeWhitespace(ma.getValue()));
			} else if ("hand_1".equals(field) || "hand_4".equals(field) || "hand_7".equals(field)) {
				if (legacyMetadataFacet == null) {
					legacyMetadataFacet = new LegacyMetadata();
				}
				if (legacyMetadataFacet.getHands() == null) {
					legacyMetadataFacet.setHands("");
				}
				String value = normalizeWhitespace(ma.getValue());
				if (!legacyMetadataFacet.getHands().contains(value)) {
					legacyMetadataFacet.setHands(legacyMetadataFacet.getHands() + " " + value);
				}
				legacyMetadataFacet.setHands(StringUtils.trimToNull(legacyMetadataFacet.getHands()));
			} else if ("work_genetic_level_goethe".equals(field) || "work_genetic_level_custom".equals(field)) {
				if (legacyMetadataFacet == null) {
					legacyMetadataFacet = new LegacyMetadata();
				}
				if (legacyMetadataFacet.getGeneticLevel() == null) {
					legacyMetadataFacet.setGeneticLevel("");
				}
				legacyMetadataFacet.setGeneticLevel(StringUtils.trimToNull(legacyMetadataFacet.getGeneticLevel()
						+ " " + normalizeWhitespace(ma.getValue())));
			} else if ("remarks".equals(field)) {
				if (legacyMetadataFacet == null) {
					legacyMetadataFacet = new LegacyMetadata();
				}
				legacyMetadataFacet.setRemarks(normalizeWhitespace(ma.getValue()));
			} else if ("dating_normalized".equals(field) || "dating_given".equals(field)) {
				if (datingFacet == null) {
					datingFacet = new DocumentDating();
				}
				if (datingFacet.getRemarks() == null) {
					datingFacet.setRemarks("");
				}
				datingFacet.setRemarks(StringUtils.trimToNull(datingFacet.getRemarks() + " "
						+ normalizeWhitespace(ma.getValue())));
			}

		}
		List<HierarchyNodeFacet> facets = Lists.newArrayList();
		facets.add(archiveFacet);
		if (printReferenceFacet != null) {
			facets.add(printReferenceFacet);
		}
		if (legacyMetadataFacet != null) {
			facets.add(legacyMetadataFacet);
		}
		if (datingFacet != null) {
			facets.add(datingFacet);
		}
		return facets;
	}

	private static String normalizeWhitespace(String str) {
		return str.replaceAll("\\s+", " ");
	}
}
