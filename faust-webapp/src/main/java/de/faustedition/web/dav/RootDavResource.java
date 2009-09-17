package de.faustedition.web.dav;

import java.util.List;

import com.bradmcevoy.http.Resource;
import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Repository;

public class RootDavResource extends CollectionDavResourceBase {

	protected RootDavResource(DavResourceFactory factory) {
		super(factory);
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public Resource child(String childName) {
		Repository repository = Repository.find(factory.getDbSessionFactory().getCurrentSession(), childName);
		return (repository == null ? null : new RepositoryDavResource(factory, repository));
	}

	@Override
	public List<? extends Resource> getChildren() {
		return Lists.transform(Repository.find(factory.getDbSessionFactory().getCurrentSession()), factory.repositoryResourceCreator);
	}

	@Override
	public Object getLockResource() {
		return this.getClass();
	}
}
