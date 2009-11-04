package de.swkk.metadata;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.init.BootstrapPostProcessor;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.util.LoggingUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;
import de.swkk.metadata.inventory.FaustInventory;
import de.swkk.metadata.inventory.MetadataFieldMapping;

public class MetadataBootstrapPostProcessor implements BootstrapPostProcessor {

	@Autowired
	private SessionFactory dbSessionFactory;

	private FaustInventory faustInventory;
	private ArchiveDatabase archiveDatabase;

	@Override
	public void afterBootstrapping() {
		Session session = dbSessionFactory.getCurrentSession();
		Repository repository = Repository.find(session, "GSA");
		if (repository == null) {
			return;
		}

		LoggingUtil.LOG.info("Importing SWKK metadata");
		MetadataFieldMapping mapping = new MetadataFieldMapping();
		for (AllegroRecord record : faustInventory) {
			GSACallNumber callNumber = faustInventory.getCallNumber(record);
			for (ArchiveDatabaseRecord archiveDbRecord : archiveDatabase.filter(callNumber)) {
				Integer portfolioId = archiveDbRecord.getIdentNum();
				LoggingUtil.LOG.info(callNumber + " --> " + portfolioId);
				Portfolio portfolio = Portfolio.find(session, repository, Integer.toString(portfolioId));
				if (portfolio == null) {
					continue;
				}

				Map<String, MetadataAssignment> existing = new HashMap<String, MetadataAssignment>();
				for (MetadataAssignment assignment : MetadataAssignment.find(session, Portfolio.class.getName(), portfolio.getId())) {
					existing.put(assignment.getField(), assignment);
				}

				for (MetadataAssignment assignment : mapping.map(record, Portfolio.class.getName(), portfolio.getId())) {
					if (existing.containsKey(assignment.getField())) {
						MetadataAssignment existingAssignment = existing.get(assignment.getField());
						if (!existingAssignment.getValue().contains(assignment.getValue())) {
							existingAssignment.setValue(existingAssignment.getValue() + "\n" + assignment.getValue());
						}
					} else {
						assignment.create(session);
					}
				}
			}
		}

	}

	@PostConstruct
	public void init() throws Exception {
		faustInventory = new FaustInventory();
		archiveDatabase = new ArchiveDatabase();
	}
}
