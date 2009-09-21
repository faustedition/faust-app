package de.faustedition.web.manuscript;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.SessionFactory;

import de.faustedition.model.manuscript.Repository;
import de.faustedition.web.PageBase;

public class ManuscriptsPage extends PageBase
{

	@SpringBean
	private SessionFactory dbSessionFactory;

	private transient List<Repository> repositories;

	public ManuscriptsPage()
	{
		super();
		repositories = Repository.find(dbSessionFactory.getCurrentSession());
		add(new RepositoryDataView("repositories"));
	}

	@Override
	public String getPageTitle()
	{
		return "Manuskripte";
	}

	private class RepositoryDataView extends DataView<Repository>
	{
		public RepositoryDataView(String id)
		{
			super(id, new ListDataProvider<Repository>(repositories));
		}

		@Override
		protected void populateItem(final Item<Repository> item)
		{
			Repository repository = item.getModel().getObject();
			BookmarkablePageLink<? extends Page> link = RepositoryPage.getLink("repositoryLink", repository);
			link.add(new Label("repositoryName", new PropertyModel<String>(repository, "name")));
			item.add(link);
		}

	}
}
