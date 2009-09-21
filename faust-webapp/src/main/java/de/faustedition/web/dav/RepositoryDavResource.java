package de.faustedition.web.dav;

import java.util.List;

import com.bradmcevoy.http.Resource;
import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;

public class RepositoryDavResource extends CollectionDavResourceBase
{

	private Repository repository;

	public RepositoryDavResource(DavResourceFactory factory, Repository repository)
	{
		super(factory);
		this.repository = repository;
	}

	@Override
	public String getName()
	{
		return repository.getName();
	}

	@Override
	public Resource child(String childName)
	{
		Portfolio portfolio = Portfolio.find(factory.getDbSessionFactory().getCurrentSession(), repository, childName);
		return portfolio == null ? null : new PortfolioDavResource(factory, portfolio);
	}

	@Override
	public List<? extends Resource> getChildren()
	{
		return Lists.transform(Portfolio.find(factory.getDbSessionFactory().getCurrentSession(), repository), factory.portfolioResourceCreator);
	}

	@Override
	public Object getLockResource()
	{
		return repository;
	}
}
