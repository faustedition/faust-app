package de.swkk.metadata;

import static de.faustedition.model.XmlDocument.FAUST_NS_URI;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.commons.lang.time.StopWatch;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.model.XmlDocument;
import de.faustedition.model.repository.RepositoryUtil;
import de.faustedition.model.repository.RepositoryXmlDocument;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;
import de.swkk.metadata.inventory.FaustInventory;
import de.swkk.metadata.inventory.MetadataRecord;

@Service
public class MetadataImportTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MetadataImportTask.class);

	@Autowired
	private Repository repository;

	@Override
	public void run() {
		Session session = null;
		try {
			session = RepositoryUtil.login(repository, RepositoryUtil.XML_WS);

			QueryObjectModelFactory qf = session.getWorkspace().getQueryManager().getQOMFactory();
			Value fileName = session.getValueFactory().createValue("metadata.xml");
			Selector source = qf.selector(JcrConstants.NT_FILE, "f");
			Constraint constraint = qf.comparison(qf.nodeLocalName("f"), JCR_OPERATOR_EQUAL_TO, qf.literal(fileName));
			for (Node node : JcrUtils.getNodes(qf.createQuery(source, constraint, null, null).execute())) {
				LOG.debug("Skipping metadata import; found '" + node.getPath() + "'");
				return;
			}

			LOG.info("Importing metadata ...");
			StopWatch sw = new StopWatch();
			sw.start();
			doImport(session);
			sw.stop();
			LOG.info("Metadata imported in " + sw);
		} catch (Exception e) {
			ErrorUtil.fatal(e, "Fatal error while importing metadata");
		} finally {
			RepositoryUtil.logoutQuietly(session);
		}

	}

	public void doImport(Session session) throws IOException, RepositoryException, SAXException {
		FaustInventory faustInventory = FaustInventory.parse();
		ArchiveDatabase archiveDatabase = ArchiveDatabase.parse();

		for (AllegroRecord allegroRecord : faustInventory) {
			GSACallNumber callNumber = faustInventory.getCallNumber(allegroRecord);
			MetadataRecord newMetadata = MetadataRecord.map(allegroRecord);
			newMetadata.remove("callnumber_old");
			for (ArchiveDatabaseRecord archiveDbRecord : archiveDatabase.filter(callNumber)) {
				newMetadata.put("callnumber_old", archiveDbRecord.getCallNumber().toString());
				String identNum = Integer.toString(archiveDbRecord.getIdentNum());
				
				String portfolioPath = "/GSA/" + identNum;
				if (!session.nodeExists(portfolioPath)) {
					LOG.warn("Portfolio '{}' does not exist in repository", portfolioPath);
					continue;
				}
				
				LOG.debug("Importing metadata for GSA signature '{}' to '{}'", callNumber, portfolioPath);
				Node portfolioNode = session.getNode(portfolioPath);

				RepositoryXmlDocument repositoryDocument = null;
				Document dom = null;

				if (portfolioNode.hasNode("metadata.xml")) {
					repositoryDocument = new RepositoryXmlDocument(portfolioNode.getNode("metadata.xml"));
					dom = repositoryDocument.getDocument();
				} else {
					dom = new XmlDocument().getDom();
					dom.appendChild(dom.createElementNS(FAUST_NS_URI, "metadata"));
				}

				MetadataRecord record = MetadataRecord.fromXml(dom.getDocumentElement());
				record.merge(newMetadata);
				record.put("callnumber", identNum);

				XMLUtil.removeChildren(dom.getDocumentElement());
				record.toXml(dom.getDocumentElement());

				if (repositoryDocument == null) {
					RepositoryXmlDocument.create(portfolioNode, "metadata.xml", dom);
				} else {
					repositoryDocument.setDocument(dom);
				}
				session.save();
			}
		}
	}
}
