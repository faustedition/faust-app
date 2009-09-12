package de.faustedition.web.dav;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.google.common.base.Function;

import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.TranscriptionDocumentFactory;

public class DavResourceFactory implements ResourceFactory {
	private static final String DAV_SERVLET_PATH = "/dav";
	@Autowired
	private SessionFactory dbSessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private TranscriptionDocumentFactory transcriptionDocumentFactory;

	public SessionFactory getDbSessionFactory() {
		return dbSessionFactory;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public TranscriptionDocumentFactory getTranscriptionDocumentFactory() {
		return transcriptionDocumentFactory;
	}

	@Override
	public Resource getResource(String host, String path) {
		Resource resource = new RootDavResource(this);
		for (String part : Path.path(StringUtils.strip(StringUtils.substringAfterLast(path, DAV_SERVLET_PATH), "/")).getParts()) {
			if ((resource == null) || !(resource instanceof CollectionResource)) {
				return null;
			}
			resource = ((CollectionResource) resource).child(part);
		}
		return resource;
	}

	@Override
	public String getSupportedLevels() {
		return "1";
	}

	protected final Function<Manuscript, TranscriptionDavResource> transcriptionResourceCreator = new Function<Manuscript, TranscriptionDavResource>() {

		@Override
		public TranscriptionDavResource apply(Manuscript from) {
			return new TranscriptionDavResource(DavResourceFactory.this, from);
		}
	};
	protected Function<Portfolio, PortfolioDavResource> portfolioResourceCreator = new Function<Portfolio, PortfolioDavResource>() {

		@Override
		public PortfolioDavResource apply(Portfolio from) {
			return new PortfolioDavResource(DavResourceFactory.this, from);
		}
	};
	protected Function<Repository, RepositoryDavResource> repositoryResourceCreator = new Function<Repository, RepositoryDavResource>() {

		@Override
		public RepositoryDavResource apply(Repository from) {
			return new RepositoryDavResource(DavResourceFactory.this, from);
		}
	};
}
