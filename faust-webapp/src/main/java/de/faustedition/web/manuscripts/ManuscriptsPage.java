package de.faustedition.web.manuscripts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.PropertyModel;

import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.transcription.Repository;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.FaustApplication;

public class ManuscriptsPage extends AbstractPage {

	public ManuscriptsPage() {
		super();
		add(new RepositoryDataView("repositories"));
	}

	@Override
	public String getPageTitle() {
		return "Manuskripte/ Best\u00e4nde";
	}

	private class RepositoryDataView extends DataView<Repository> {
		public RepositoryDataView(String id) {
			super(id, new ListDataProvider<Repository>(FaustApplication.get().doInContentStore(new ContentStoreCallback<List<Repository>>() {

				@Override
				public List<Repository> inStore(Session session) throws RepositoryException {
					return new ArrayList<Repository>(Repository.find(session));
				}

			})));
		}

		@Override
		protected void populateItem(final Item<Repository> item) {
			Repository repository = item.getModel().getObject();
			BookmarkablePageLink<RepositoryPage> link = RepositoryPage.getLink("repositoryLink", repository);
			link.add(new Label("repositoryName", new PropertyModel<String>(repository, "name")));
			item.add(link);
		}

	}
}
