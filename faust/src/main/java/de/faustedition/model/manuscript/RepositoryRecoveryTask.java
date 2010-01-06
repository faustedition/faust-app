package de.faustedition.model.manuscript;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
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
import de.faustedition.model.facsimile.FacsimileImageResolution;
import de.faustedition.model.facsimile.FacsimileReference;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.repository.RepositoryBackupTask;
import de.faustedition.model.repository.RepositoryFile;
import de.faustedition.model.repository.RepositoryFolder;
import de.faustedition.model.repository.RepositoryUtil;
import de.faustedition.model.tei.EncodedDocument;
import de.faustedition.model.tei.EncodedDocumentManager;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@Service
public class RepositoryRecoveryTask implements Runnable {
	private static final String FACSIMILE_SUFFIX = FacsimileImageResolution.HIGH.getSuffix();

	@Autowired
	private EncodedDocumentManager encodedDocumentManager;

	@Autowired
	@Qualifier("backup")
	private File backupDirectory;

	@Autowired
	private javax.jcr.Repository repository;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TaskExecutor taskExecutor;
	
	@PostConstruct
	public void scheduleRecovery() {
		// schedule recovery
	}

	public void run() {
		javax.jcr.Session repoSession = null;
		try {
			RepositoryFolder appNode = RepositoryUtil.appNode(repoSession = RepositoryUtil.login(repository));
			if (appNode.getNode().hasNodes()) {
				LoggingUtil.LOG.debug("Repository is not empty; skip recovery");
				return;
			}
			if (recoverFromBackup(repoSession)) {
				LoggingUtil.LOG.info("Repository recovered from backup");
				return;
			}

			migrateFromRdbms(repoSession);
			LoggingUtil.LOG.info("Repository recovered from RDBMS");
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

			Node appNode = RepositoryUtil.appNode(repoSession).getNode();
			repoSession.save();
			repoSession.getWorkspace().importXML(appNode.getPath(), backupStream, IMPORT_UUID_COLLISION_THROW);
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
				FacsimileReference.create(pageFolder, name + FACSIMILE_SUFFIX, facsimile.getImagePath());

				Transcription transcription = Transcription.find(session, facsimile);
				if (transcription != null) {
					EncodedDocument teiDocument = transcription.buildTEIDocument(encodedDocumentManager);

					TranscriptionStatus status = TranscriptionStatus.extract(teiDocument);
					if (TranscriptionStatus.EMPTY.equals(status)) {
						Element text = teiDocument.findNode("/:TEI/:text", Element.class);
						if (text != null && XMLUtil.hasText(text)) {
							Element teiHeader = teiDocument.findNode("/:TEI/:teiHeader", Element.class);
							Element revisionDesc = EncodedDocument.findNode(teiHeader,
									"./:revisionDesc", Element.class);

							Document document = teiDocument.getDocument();
							if (revisionDesc == null) {
								revisionDesc = document.createElementNS(EncodedDocument.TEI_NS_URI,
										"revisionDesc");
								teiHeader.appendChild(revisionDesc);
							}
							Element change = document.createElementNS(EncodedDocument.TEI_NS_URI,
									"change");
							revisionDesc.appendChild(change);

							String today = DateFormatUtils.ISO_DATE_FORMAT.format(System
									.currentTimeMillis());
							change.setAttributeNS(EncodedDocument.TEI_NS_URI, "when", today);
							change.setAttributeNS(EncodedDocument.TEI_NS_URI, "who", "system");
							if (Pattern.matches("^0*1$", name)) {
								change.setTextContent("Rohzustand");
								status = TranscriptionStatus.RAW;
							} else {
								change.setTextContent("kodiert");
								status = TranscriptionStatus.ENCODED;
							}
						}
					}
					byte[] data = XMLUtil.serialize(teiDocument.getDocument(), false);
					ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
					RepositoryFile file = RepositoryFile.create(pageFolder, name + ".xml", dataStream);
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
