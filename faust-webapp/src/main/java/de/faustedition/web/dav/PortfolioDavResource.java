package de.faustedition.web.dav;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bradmcevoy.http.Resource;
import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;

public class PortfolioDavResource extends CollectionDavResourceBase {

	private final Portfolio portfolio;

	protected PortfolioDavResource(DavResourceFactory factory, Portfolio portfolio) {
		super(factory);
		this.portfolio = portfolio;
	}

	@Override
	public String getName() {
		return portfolio.getName();
	}

	@Override
	public Resource child(String childName) {
		childName = StringUtils.strip(StringUtils.removeStart(childName, portfolio.getName()), "_-:");
		Manuscript manuscript = Manuscript.find(factory.getDbSessionFactory().getCurrentSession(), portfolio, childName);
		return (manuscript == null ? null : new TranscriptionDavResource(factory, manuscript));
	}

	@Override
	public List<? extends Resource> getChildren() {
		return Lists.transform(Manuscript.find(factory.getDbSessionFactory().getCurrentSession(), portfolio), factory.transcriptionResourceCreator);
	}
}
