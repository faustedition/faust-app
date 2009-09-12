package de.faustedition.web.manuscript;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.facsimile.FacsimileImage;

public class ManuscriptPage extends AbstractPage {

	@SpringBean
	private SessionFactory dbSessionFactory;
	
	private Repository repository;
	private Portfolio portfolio;
	private Manuscript manuscript;
	private Facsimile facsimile;

	public ManuscriptPage(PageParameters parameters) {
		super();

		final String repositoryName = parameters.getString("0");
		final String portfolioName = parameters.getString("1");
		final String transcriptionName = parameters.getString("2");
		if (repositoryName == null || portfolioName == null || transcriptionName == null) {
			throw new InvalidUrlException();
		}
		
		Session session = dbSessionFactory.getCurrentSession();
		FaustApplication.assertFound(repository = Repository.find(session, repositoryName));
		FaustApplication.assertFound(portfolio = Portfolio.find(session, repository, portfolioName));
		FaustApplication.assertFound(manuscript = Manuscript.find(session, portfolio, transcriptionName));
		FaustApplication.assertFound(facsimile = Facsimile.find(session, manuscript, transcriptionName));

		add(new Label("manuscriptHeader", new PropertyModel<String>(manuscript, "name")));
		add(new FacsimileImage("facsimile", facsimile, FacsimileImageResolution.LOW));
		// TODO: add(new DavResourceLink("davLink", manuscript));
	}

	@Override
	public String getPageTitle() {
		return (manuscript == null ? "" : manuscript.getName());
	}

	public static BookmarkablePageLink<? extends Page> getLink(String id, Manuscript manuscript) {
		PageParameters parameters = new PageParameters();
		parameters.add("0", manuscript.getPortfolio().getRepository().getName());
		parameters.add("1", manuscript.getPortfolio().getName());
		parameters.add("2", manuscript.getName());
		return new BookmarkablePageLink<Page>(id, ManuscriptPage.class, parameters);
	}

}
