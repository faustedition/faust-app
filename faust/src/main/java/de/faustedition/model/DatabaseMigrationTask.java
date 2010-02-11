package de.faustedition.model;

import static de.faustedition.model.tei.EncodedTextDocument.TEI_NS_URI;
import static de.faustedition.model.tei.EncodedTextDocument.xpath;
import static de.faustedition.model.xmldb.NodeListIterable.singleResult;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

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
import org.w3c.dom.Node;

import de.faustedition.model.db.Facsimile;
import de.faustedition.model.db.Manuscript;
import de.faustedition.model.db.Portfolio;
import de.faustedition.model.db.Repository;
import de.faustedition.model.db.Transcription;
import de.faustedition.model.facsimile.FacsimileReference;
import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.tei.EncodedTextDocumentManager;
import de.faustedition.model.xmldb.XmlDbManager;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;
import de.swkk.metadata.MetadataImportTask;

@Service
public class DatabaseMigrationTask implements Runnable {
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	@Qualifier("hibernateTransactionManager")
	private PlatformTransactionManager transactionManager;

	@Autowired
	private XmlDbManager xmlDbManager;

	@Autowired
	private EncodedTextDocumentManager documentManager;

	@Autowired
	private MetadataImportTask metadataImportTask;

	@PostConstruct
	public void scheduleMigration() {
		Executors.newSingleThreadExecutor().execute(this);
	}

	@Override
	public void run() {
		if (singleResult(XmlDocument.xpath("//f:resource[starts-with(text(), 'Witness/')]"), xmlDbManager.resources(),
				Element.class) != null) {
			LoggingUtil.LOG.debug("XML database contains witnesses; skip RDBMS migration");
		} else {
			StopWatch sw = new StopWatch();
			sw.start();
			migrateFromRdbms();
			sw.stop();
			LoggingUtil.LOG.info("Repository migrated from RDBMS in " + sw);
		}

		metadataImportTask.run();
	}

	private void migrateFromRdbms() {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				Session session = sessionFactory.getCurrentSession();

				LoggingUtil.LOG.info("Repository recovery; migrating data from RDBMS");
				for (Repository repository : Repository.find(session)) {
					LoggingUtil.LOG.info("Migrating " + repository.getName());
					for (Portfolio portfolio : Portfolio.find(session, repository)) {
						LoggingUtil.LOG.debug("Migrating " + portfolio);
						migratePortfolio(session, portfolio);
						session.flush();
						session.clear();
					}
				}
			}
		});
	}

	private void migratePortfolio(Session session, Portfolio portfolio) {
		for (Manuscript manuscript : Manuscript.find(session, portfolio)) {
			String name = manuscript.getName();
			Facsimile facsimile = Facsimile.find(session, manuscript, name);
			if (facsimile != null) {
				Transcription transcription = Transcription.find(session, facsimile);
				if (transcription != null) {
					EncodedTextDocument document = transcription.buildTEIDocument(documentManager);
					FacsimileReference.writeTo(document, Collections.singletonList(new FacsimileReference(
							facsimile.getImagePath())));

					Document xml = document.getDom();
					Element textEl = singleResult(xpath("//tei:text"), xml, Element.class);
					if (singleResult(xpath("//tei:revisionDesc/tei:change"), xml, Node.class) == null) {
						if (textEl != null && XMLUtil.hasText(textEl)) {
							Element teiHeader = singleResult(xpath("//tei:teiHeader"), xml,
									Element.class);
							Element revisionDesc = singleResult(
									xpath("//tei:teiHeader/tei:revisionDesc"), xml,
									Element.class);

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

					String path = String.format("Witness/%s/%s/%s.xml", portfolio.getRepository().getName(),
							portfolio.getName(), name);
					xmlDbManager.put(URI.create(path), xml);
					LoggingUtil.LOG.debug("Migrated " + path);
					session.flush();
					session.clear();
				}
			}

		}
	}
}
