package de.faustedition.model.db;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.model.document.TranscriptionStatus;
import de.faustedition.model.facsimile.Facsimile;
import de.faustedition.model.repository.RepositoryDocument;
import de.faustedition.model.repository.RepositoryFolder;
import de.faustedition.model.repository.RepositoryUtil;
import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.tei.EncodedTextDocumentManager;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.URIUtil;
import de.faustedition.util.XMLUtil;

@Service
public class DatabaseMigrationTask implements Runnable {
	@Autowired
	private javax.jcr.Repository repository;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private EncodedTextDocumentManager documentManager;

	@PostConstruct
	public void scheduleMigration() {
		Executors.newSingleThreadExecutor().execute(this);
	}

	@Override
	public void run() {
		javax.jcr.Session repoSession = null;
		try {
			repoSession = RepositoryUtil.login(repository, RepositoryUtil.XML_WS);
			if (RepositoryUtil.isNotEmpty(repoSession)) {
				LoggingUtil.LOG.debug("Repository is not empty; skip RDBMS migration");
				return;
			}

			StopWatch sw = new StopWatch();
			sw.start();
			migrateFromRdbms(repoSession);
			sw.stop();
			LoggingUtil.LOG.info("Repository migrated from RDBMS in " + sw);
		} catch (RepositoryException e) {
			throw ErrorUtil.fatal(e, "Repository error while recovering repository: %s", e.getMessage());
		} finally {
			RepositoryUtil.logoutQuietly(repoSession);
		}
	}

	private void migrateFromRdbms(final javax.jcr.Session repoSession) {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					Node root = repoSession.getRootNode();
					Session session = sessionFactory.getCurrentSession();

					LoggingUtil.LOG.info("Repository recovery; migrating data from RDBMS");
					for (Repository repository : Repository.find(session)) {
						RepositoryFolder repoFolder = RepositoryFolder.create(root, repository.getName());
						LoggingUtil.LOG.info("Migrating " + repoFolder);
						migrateRepository(session, repository, repoFolder);
					}

					repoSession.save();
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
					EncodedTextDocument document = transcription.buildTEIDocument(documentManager);
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

					if (TranscriptionStatus.EMPTY.equals(TranscriptionStatus.extract(document))) {
						if (textEl != null && XMLUtil.hasText(textEl)) {
							Element teiHeader = document.findNode("/:TEI/:teiHeader", Element.class);
							Element revisionDesc = EncodedTextDocument.findNode(teiHeader,
									"./:revisionDesc", Element.class);

							if (revisionDesc == null) {
								revisionDesc = xml.createElementNS(TEI_NS_URI, "revisionDesc");
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
							} else {
								change.setTextContent("kodiert");
							}
						}
					}
					RepositoryDocument file = RepositoryDocument.create(pageFolder, name + ".xml", document);

					session.flush();
					session.clear();
					folder.save();
					LoggingUtil.LOG.debug("Migrated " + file);
				}
			}

		}
	}

	// private Collection<HierarchyNodeFacet> buildPortfolioFacets(Session
	// session, Repository repository, Portfolio portfolio) {
	// ArchiveReference archiveFacet = new ArchiveReference();
	// archiveFacet.setRepository(repository.getName());
	// archiveFacet.setCallnumber(portfolio.getName());
	//
	// PrintReference printReferenceFacet = null;
	// LegacyMetadata legacyMetadataFacet = null;
	// DocumentDating datingFacet = null;
	//
	// for (MetadataAssignment ma : MetadataAssignment.find(session,
	// Portfolio.class.getName(), portfolio.getId())) {
	// String field = ma.getField();
	// if ("callnumber_old".equals(field)) {
	// String[] callnumbers = StringUtils.split(ma.getValue());
	// for (int cc = 0; cc < callnumbers.length; cc++) {
	// callnumbers[cc] =
	// StringUtils.substringBeforeLast(callnumbers[cc].trim(), "*");
	// }
	//
	// archiveFacet.setLegacyCallnumber("");
	// for (String cn : callnumbers) {
	// if (!archiveFacet.getLegacyCallnumber().contains(cn)) {
	// archiveFacet.setLegacyCallnumber(archiveFacet.getLegacyCallnumber() +
	// " " + cn);
	// }
	// }
	// archiveFacet.setLegacyCallnumber(StringUtils.trimToNull(archiveFacet.getLegacyCallnumber()));
	// } else if ("id_weimarer_ausgabe".equals(field) ||
	// "print_weimarer_ausgabe".equals(field)
	// || "print_weimarer_ausgabe_additional".equals(field)) {
	// String id =
	// normalizeWhitespace(StringUtils.remove(StringUtils.remove("oS",
	// ma.getValue()), "-"))
	// .trim();
	// if (id.length() > 0) {
	// if (printReferenceFacet == null) {
	// printReferenceFacet = new PrintReference();
	// }
	// if (printReferenceFacet.getReferenceWeimarerAusgabe() == null) {
	// printReferenceFacet.setReferenceWeimarerAusgabe("");
	// }
	// printReferenceFacet.setReferenceWeimarerAusgabe(StringUtils.trimToNull(printReferenceFacet
	// .getReferenceWeimarerAusgabe()
	// + " " + id));
	// }
	// } else if ("manuscript_reference_weimarer_ausgabe".equals(field)) {
	// if (printReferenceFacet == null) {
	// printReferenceFacet = new PrintReference();
	// }
	// printReferenceFacet.setManuscriptReferenceWeimarerAusgabe(normalizeWhitespace(ma.getValue()));
	// } else if ("id_paralipomenon_weimarer_ausgabe".equals(field)) {
	// if (printReferenceFacet == null) {
	// printReferenceFacet = new PrintReference();
	// }
	// printReferenceFacet.setParalipomenonReferenceWeimarerAusgabe(normalizeWhitespace(ma.getValue()));
	// } else if ("record_number".equals(field)) {
	// if (legacyMetadataFacet == null) {
	// legacyMetadataFacet = new LegacyMetadata();
	// }
	// legacyMetadataFacet.setRecordNumber(normalizeWhitespace(ma.getValue()));
	// } else if ("hand_1".equals(field) || "hand_4".equals(field) ||
	// "hand_7".equals(field)) {
	// if (legacyMetadataFacet == null) {
	// legacyMetadataFacet = new LegacyMetadata();
	// }
	// if (legacyMetadataFacet.getHands() == null) {
	// legacyMetadataFacet.setHands("");
	// }
	// String value = normalizeWhitespace(ma.getValue());
	// if (!legacyMetadataFacet.getHands().contains(value)) {
	// legacyMetadataFacet.setHands(legacyMetadataFacet.getHands() + " " +
	// value);
	// }
	// legacyMetadataFacet.setHands(StringUtils.trimToNull(legacyMetadataFacet.getHands()));
	// } else if ("work_genetic_level_goethe".equals(field) ||
	// "work_genetic_level_custom".equals(field)) {
	// if (legacyMetadataFacet == null) {
	// legacyMetadataFacet = new LegacyMetadata();
	// }
	// if (legacyMetadataFacet.getGeneticLevel() == null) {
	// legacyMetadataFacet.setGeneticLevel("");
	// }
	// legacyMetadataFacet.setGeneticLevel(StringUtils.trimToNull(legacyMetadataFacet.getGeneticLevel()
	// + " " + normalizeWhitespace(ma.getValue())));
	// } else if ("remarks".equals(field)) {
	// if (legacyMetadataFacet == null) {
	// legacyMetadataFacet = new LegacyMetadata();
	// }
	// legacyMetadataFacet.setRemarks(normalizeWhitespace(ma.getValue()));
	// } else if ("dating_normalized".equals(field) ||
	// "dating_given".equals(field)) {
	// if (datingFacet == null) {
	// datingFacet = new DocumentDating();
	// }
	// if (datingFacet.getRemarks() == null) {
	// datingFacet.setRemarks("");
	// }
	// datingFacet.setRemarks(StringUtils.trimToNull(datingFacet.getRemarks()
	// + " "
	// + normalizeWhitespace(ma.getValue())));
	// }
	//
	// }
	// List<HierarchyNodeFacet> facets = Lists.newArrayList();
	// facets.add(archiveFacet);
	// if (printReferenceFacet != null) {
	// facets.add(printReferenceFacet);
	// }
	// if (legacyMetadataFacet != null) {
	// facets.add(legacyMetadataFacet);
	// }
	// if (datingFacet != null) {
	// facets.add(datingFacet);
	// }
	// return facets;
	// }
	//
	// private static String normalizeWhitespace(String str) {
	// return str.replaceAll("\\s+", " ");
	// }
}
