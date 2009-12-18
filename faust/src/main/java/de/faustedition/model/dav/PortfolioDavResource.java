package de.faustedition.model.dav;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;

public class PortfolioDavResource extends CollectionDavResourceBase implements PutableResource
{

	private final Portfolio portfolio;

	protected PortfolioDavResource(DavResourceFactory factory, Portfolio portfolio)
	{
		super(factory);
		this.portfolio = portfolio;
	}

	@Override
	public String getName()
	{
		return portfolio.getName();
	}

	@Override
	public Resource child(String childName)
	{
		return findManuscriptDavResource(childName);
	}

	@Override
	public List<? extends Resource> getChildren()
	{
		return Lists.transform(Manuscript.find(factory.getDbSessionFactory().getCurrentSession(), portfolio), factory.transcriptionResourceCreator);
	}

	@Override
	public Object getLockResource()
	{
		return portfolio;
	}

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException
	{
		TranscriptionDavResource manuscriptDavResource = findManuscriptDavResource(newName);
		if (manuscriptDavResource == null)
		{
			throw new UnsupportedOperationException();
		}
		manuscriptDavResource.update(inputStream);
		return manuscriptDavResource;
	}

	protected TranscriptionDavResource findManuscriptDavResource(String name)
	{
		name = StringUtils.strip(StringUtils.removeEnd(StringUtils.removeStart(name, portfolio.getName()), ".xml"), "_-:");
		Manuscript manuscript = Manuscript.find(factory.getDbSessionFactory().getCurrentSession(), portfolio, name);
		return (manuscript == null ? null : new TranscriptionDavResource(factory, manuscript));

	}
}
