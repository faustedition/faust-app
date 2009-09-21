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

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.web.PageBase;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.facsimile.FacsimileImage;
import de.faustedition.web.metadata.MetadataTable;
import de.faustedition.web.util.UpLink;

public class PortfolioPage extends PageBase
{

	@SpringBean
	private SessionFactory dbSessionFactory;

	private Repository repository;
	private Portfolio portfolio;
	private List<MetadataAssignment> metadata;
	private List<Manuscript> manuscripts;

	public PortfolioPage(PageParameters parameters)
	{
		super();
		final String repositoryName = parameters.getString("0");
		final String portfolioName = parameters.getString("1");
		if (repositoryName == null || portfolioName == null)
		{
			throw new InvalidUrlException();
		}

		Session session = dbSessionFactory.getCurrentSession();
		FaustApplication.assertFound(repository = Repository.find(session, repositoryName));
		FaustApplication.assertFound(portfolio = Portfolio.find(session, repository, portfolioName));
		metadata = MetadataAssignment.find(session, Portfolio.class.getName(), portfolio.getId());
		manuscripts = Manuscript.find(session, portfolio);

		add(new UpLink("repositoryLink", RepositoryPage.getLink("upLink", portfolio.getRepository())));
		add(new ManuscriptDataView("manuscripts"));
		add(new MetadataTable("metadata", metadata));
	}

	@Override
	public String getPageTitle()
	{
		return (portfolio == null ? "" : portfolio.getName());
	}

	private class ManuscriptDataView extends GridView<Manuscript>
	{

		public ManuscriptDataView(String id)
		{
			super(id, new ListDataProvider<Manuscript>(manuscripts));
			setColumns(3);
		}

		@Override
		protected void populateItem(Item<Manuscript> item)
		{
			item.add(new ManuscriptPanel("manuscript", item.getModel()));
		}

		@Override
		protected void populateEmptyItem(Item<Manuscript> item)
		{
			item.add(new Label("manuscript", ""));
		}

	}

	private class ManuscriptPanel extends Panel
	{

		public ManuscriptPanel(String id, IModel<Manuscript> model)
		{
			super(id, model);
			final Manuscript manuscript = model.getObject();

			final BookmarkablePageLink<? extends Page> link = ManuscriptPage.getLink("manuscriptLink", manuscript);
			link.add(new FacsimileImage("facsimileThumb", Facsimile.find(dbSessionFactory.getCurrentSession(), manuscript, manuscript.getName()), FacsimileImageResolution.THUMB));
			add(link);

			add(new Label("manuscriptName", new PropertyModel<String>(manuscript, "name")));
		}
	}

	public static BookmarkablePageLink<? extends Page> getLink(String id, Portfolio portfolio)
	{
		PageParameters parameters = new PageParameters();
		parameters.put("0", portfolio.getRepository().getName());
		parameters.put("1", portfolio.getName());
		return new BookmarkablePageLink<Page>(id, PortfolioPage.class, parameters);
	}
}
