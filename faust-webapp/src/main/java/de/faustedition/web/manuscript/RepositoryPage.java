package de.faustedition.web.manuscript;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.web.PageBase;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.util.UpLink;

public class RepositoryPage extends PageBase
{

	@SpringBean
	private SessionFactory dbSessionFactory;

	private Repository repository;
	private List<Portfolio> portfolios;

	public RepositoryPage(PageParameters parameters)
	{
		super();

		final String repositoryName = parameters.getString("0");
		if (repositoryName == null)
		{
			throw new InvalidUrlException("No repository name given");
		}

		Session session = dbSessionFactory.getCurrentSession();
		FaustApplication.assertFound(repository = Repository.find(session, repositoryName));
		portfolios = Portfolio.find(session, repository);

		add(new Label("repositoryHeader", new PropertyModel<String>(repository, "name")));
		add(new UpLink("manuscriptsLink", new BookmarkablePageLink<Page>("upLink", ManuscriptsPage.class)));
		add(new PortfolioDataView("portfolios"));
	}

	@Override
	public String getPageTitle()
	{
		return (repository == null ? "" : repository.getName());
	}

	private class PortfolioDataView extends GridView<Portfolio>
	{

		public PortfolioDataView(String id)
		{
			super(id, new ListDataProvider<Portfolio>(portfolios));
			setColumns(5);
		}

		@Override
		protected void populateItem(Item<Portfolio> item)
		{
			item.add(new PortfolioPanel("portfolioPanel", item.getModel()));
		}

		@Override
		protected void populateEmptyItem(Item<Portfolio> item)
		{
			item.add(new Label("portfolioPanel"));
		}
	}

	private class PortfolioPanel extends Panel
	{

		public PortfolioPanel(String id, IModel<Portfolio> model)
		{
			super(id, model);
			Portfolio portfolio = model.getObject();
			BookmarkablePageLink<? extends Page> link = PortfolioPage.getLink("portfolioLink", portfolio);
			link.add(new Label("portfolioName", new PropertyModel<String>(portfolio, "name")));
			add(link);
		}

	}

	public static BookmarkablePageLink<? extends Page> getLink(String id, Repository repository)
	{
		PageParameters parameters = new PageParameters();
		parameters.put("0", repository.getName());
		return new BookmarkablePageLink<Page>(id, RepositoryPage.class, parameters);
	}
}
