package de.faustedition.web.manuscripts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
import org.springframework.util.Assert;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Manuscripts;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.AbstractRepositoryObjectLinkResolver;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.RepositoryObjectLinkResolver;

public class RepositoryPage extends AbstractPage {
	public static RepositoryObjectLinkResolver LINK_RESOLVER = new AbstractRepositoryObjectLinkResolver() {

		@Override
		public BookmarkablePageLink<? extends Page> resolve(String id, Class<? extends RepositoryObject> type, String path) {
			Assert.isAssignable(Repository.class, type);
			PageParameters parameters = new PageParameters();
			String[] pathComponents = DataRepository.splitPath(path);
			parameters.add("0", pathComponents[pathComponents.length - 1]);
			return new BookmarkablePageLink<RepositoryPage>(id, RepositoryPage.class, parameters);
		}
	};
	
	private Repository repository;

	private List<Portfolio> portfolios;

	public RepositoryPage(PageParameters parameters) {
		super();

		final String repositoryName = parameters.getString("0");
		if (repositoryName == null) {
			throw new InvalidUrlException("No repository name given");
		}

		FaustApplication.get().accessDataRepository(new DataRepositoryTemplate<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				repository = Manuscripts.get(session).get(session, Repository.class, repositoryName);
				portfolios = new ArrayList<Portfolio>(repository.find(session, Portfolio.class));
				return null;
			}
		});

		add(new Label("repositoryHeader", new PropertyModel<String>(repository, "name")));
		add(new PortfolioDataView("portfolios"));
	}

	@Override
	public String getPageTitle() {
		return (repository == null ? "" : repository.getName());
	}

	private class PortfolioDataView extends GridView<Portfolio> {

		public PortfolioDataView(String id) {
			super(id, new ListDataProvider<Portfolio>(portfolios));
			setColumns(5);
		}

		@Override
		protected void populateItem(Item<Portfolio> item) {
			item.add(new PortfolioPanel("portfolioPanel", item.getModel()));
		}

		@Override
		protected void populateEmptyItem(Item<Portfolio> item) {
			item.add(new Label("portfolioPanel"));
		}
	}

	private class PortfolioPanel extends Panel {

		public PortfolioPanel(String id, IModel<Portfolio> model) {
			super(id, model);
			Portfolio portfolio = model.getObject();
			BookmarkablePageLink<? extends Page> link = FaustApplication.get().getLink("portfolioLink", portfolio);
			link.add(new Label("portfolioName", new PropertyModel<String>(portfolio, "name")));
			add(link);
		}

	}
}
