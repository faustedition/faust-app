package de.faustedition.model.search;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.Manuscripts;
import de.faustedition.util.LoggingUtil;

public class MetadataIndexer {

	@Autowired
	private DataRepository dataRepository;

	private CompassTemplate compassTemplate;
	private ResourceFactory compassResourceFactory;

	@Autowired
	public void setCompass(Compass compass) {
		this.compassTemplate = new CompassTemplate(compass);
		this.compassResourceFactory = compass.getResourceFactory();
	}

	public void index() {
		compassTemplate.execute(new CompassCallbackWithoutResult() {

			@Override
			protected void doInCompassWithoutResult(final CompassSession compassSession) throws CompassException {
				try {
					purgeMetadata(compassSession);
					dataRepository.execute(new DataRepositoryTemplate<Object>() {

						@Override
						public Object doInSession(Session repositorySession) throws RepositoryException {
							for (Repository repository : Manuscripts.get(repositorySession).find(repositorySession, Repository.class)) {
								indexMetadata(compassSession, repositorySession, repository);
								for (Portfolio portfolio : repository.find(repositorySession, Portfolio.class)) {
									indexMetadata(compassSession, repositorySession, portfolio);
									for (Transcription transcription : portfolio.find(repositorySession, Transcription.class)) {
										indexMetadata(compassSession, repositorySession, transcription);
									}
								}
							}
							return null;
						}
					});
				} catch (RepositoryException e) {
					throw new CompassException("Error indexing data repository", e);
				}
			}
		});
	}

	protected void purgeMetadata(CompassSession compassSession) {
		LoggingUtil.LOG.info("Deleting all metadata from index");
		CompassQuery allMetadataQuery = compassSession.queryBuilder().alias("metadata");
		allMetadataQuery.setAliases("metadata");
		compassSession.delete(allMetadataQuery);
	}

	protected void indexMetadata(CompassSession compassSession, Session repositorySession, RepositoryObject annotated) throws RepositoryException {
		Resource metadataResource = annotated.getMetadataResource(compassResourceFactory, repositorySession);
		if (metadataResource != null) {
			LoggingUtil.LOG.info(String.format("Indexing %s", annotated.getPath()));
			compassSession.create(metadataResource);
		}
	}
}
