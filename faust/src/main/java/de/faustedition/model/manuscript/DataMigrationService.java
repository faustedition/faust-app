package de.faustedition.model.manuscript;

import static de.faustedition.model.hierarchy.HierarchyNodeType.FILE;
import static de.faustedition.model.hierarchy.HierarchyNodeType.PAGE;
import static de.faustedition.model.hierarchy.HierarchyNodeType.REPOSITORY;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import de.faustedition.model.document.ArchiveReference;
import de.faustedition.model.document.DocumentDating;
import de.faustedition.model.document.LegacyMetadata;
import de.faustedition.model.document.PrintReference;
import de.faustedition.model.document.TranscriptionDocument;
import de.faustedition.model.document.TranscriptionStatus;
import de.faustedition.model.facsimile.Facsimile;
import de.faustedition.model.facsimile.FacsimileAssociation;
import de.faustedition.model.facsimile.FacsimileFile;
import de.faustedition.model.hierarchy.HierarchyNode;
import de.faustedition.model.hierarchy.HierarchyNodeFacet;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.tei.TEIDocument;
import de.faustedition.model.tei.TEIDocumentManager;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@Service
public class DataMigrationService {
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TEIDocumentManager teiDocumentManager;

	@Scheduled(fixedRate = (24 * 60 * 60 * 1000))
	@Transactional
	public void migrate() {
		Session session = sessionFactory.getCurrentSession();
		if (HierarchyNode.existsAny(session)) {
			return;
		}

		HierarchyNode root = HierarchyNode.rootNode(session);

		LoggingUtil.LOG.info("Migrating document data");
		HierarchyNode docs = new HierarchyNode(root, "Dokumente", FILE).save(session);
		for (Repository repo : Repository.find(session)) {
			HierarchyNode rn = new HierarchyNode(docs, repo.getName(), REPOSITORY).save(session);
			LoggingUtil.LOG.debug("Migrating " + rn.toString());
			migrateRepository(session, repo, rn);
		}

		LoggingUtil.LOG.info("Updating revision descriptions");
		String when = DateFormatUtils.ISO_DATE_FORMAT.format(System.currentTimeMillis());
		for (TranscriptionDocument facet : TranscriptionDocument.scrollAll(session)) {
			facet.updateStatus();
			if (TranscriptionStatus.EMPTY.equals(facet.getStatus())) {
				TEIDocument teiDocument = facet.getTeiDocument();
								
				Element text = teiDocument.findNode("/:TEI/:text", Element.class);
				if (text == null || !XMLUtil.hasText(text)) {
					session.flush();
					session.clear();
					continue;
				}

				LoggingUtil.LOG.debug("Updating revision descriptions of " + facet);
				Element teiHeader = teiDocument.findNode("/:TEI/:teiHeader", Element.class);
				Element revisionDesc = TEIDocument.findNode(teiHeader, "./:revisionDesc", Element.class);

				Document document = teiDocument.getDocument();
				if (revisionDesc == null) {
					revisionDesc = document.createElementNS(TEIDocument.TEI_NS_URI, "revisionDesc");
					teiHeader.appendChild(revisionDesc);
				}
				Element change = document.createElementNS(TEIDocument.TEI_NS_URI, "change");
				revisionDesc.appendChild(change);

				change.setAttributeNS(TEIDocument.TEI_NS_URI, "when", when);
				change.setAttributeNS(TEIDocument.TEI_NS_URI, "who", "system");
				if (Pattern.matches("^0*1$", facet.getFacettedNode().getName())) {
					change.setTextContent("Rohzustand");
					facet.setStatus(TranscriptionStatus.RAW);
				} else {
					change.setTextContent("kodiert");
					facet.setStatus(TranscriptionStatus.ENCODED);
				}
				facet.setDocumentData(teiDocument);
			}
			session.flush();
			session.clear();
		}

		LoggingUtil.LOG.info("Creating text node");
		new HierarchyNode(root, "Texte", FILE).save(session);
	}

	
	private void migrateRepository(Session session, Repository repository, HierarchyNode repoNode) {
		for (Portfolio portfolio : Portfolio.find(session, repository)) {
			HierarchyNode pn = new HierarchyNode(repoNode, portfolio.getName(), FILE).save(session);
			LoggingUtil.LOG.debug("Migrating " + pn);
			for (HierarchyNodeFacet facet : buildPortfolioFacets(session, repository, portfolio)) {
				facet.setFacettedNode(pn);
				facet.save(session);
			}
			migratePortfolio(session, portfolio, pn);
			session.flush();
			session.clear();
		}

	}

	private void migratePortfolio(Session session, Portfolio portfolio, HierarchyNode portfolioNode) {
		for (Manuscript manuscript : Manuscript.find(session, portfolio)) {
			HierarchyNode pn = new HierarchyNode(portfolioNode, manuscript.getName(), PAGE).save(session);

			Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
			if (facsimile != null) {
				FacsimileFile facsimileFile = new FacsimileFile();
				facsimileFile.setPath(facsimile.getImagePath());
				facsimileFile.save(session);

				FacsimileAssociation facsimileFacet = new FacsimileAssociation();
				facsimileFacet.setFacettedNode(pn);
				facsimileFacet.setFacsimileFile(facsimileFile);
				facsimileFacet.save(session);

				Transcription transcription = Transcription.find(session, facsimile);
				if (transcription != null) {
					TranscriptionDocument transcriptionFacet = new TranscriptionDocument();
					transcriptionFacet.setFacettedNode(pn);
					transcriptionFacet.setCreated(transcription.getCreated());
					transcriptionFacet.setLastModified(transcriptionFacet.getLastModified());

					TEIDocument teiDocument = transcription.buildTEIDocument(teiDocumentManager);
					transcriptionFacet.setDocumentData(XMLUtil.serialize(teiDocument.getDocument(), false));

					transcriptionFacet.save(session);
				}
			}

		}
	}

	private Collection<HierarchyNodeFacet> buildPortfolioFacets(Session session, Repository repository,
			Portfolio portfolio) {
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
