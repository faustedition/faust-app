package de.faustedition.web.manuscripts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

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

import de.faustedition.model.facsimile.FacsimileResolution;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.facsimile.FacsimileImage;

public class PortfolioPage extends AbstractPage {
	private Repository repository;
	private Portfolio portfolio;
	private List<Transcription> transcriptions;

	public static BookmarkablePageLink<PortfolioPage> getLink(String id, Repository repository, Portfolio portfolio) {
		PageParameters parameters = new PageParameters();
		parameters.add("0", repository.getName());
		parameters.add("1", portfolio.getName());
		return new BookmarkablePageLink<PortfolioPage>(id, PortfolioPage.class, parameters);
	}

	public PortfolioPage(PageParameters parameters) {
		super();
		final String repositoryName = parameters.getString("0");
		final String portfolioName = parameters.getString("1");
		if (repositoryName == null || portfolioName == null) {
			throw new InvalidUrlException();
		}

		FaustApplication.get().accessDataRepository(new DataRepositoryTemplate<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				repository = Repository.get(session, repositoryName);
				portfolio = Portfolio.get(session, repository, portfolioName);
				transcriptions = new ArrayList<Transcription>(Transcription.find(session, portfolio));
				return null;
			}
		});

		add(new Label("portfolioHeader", new PropertyModel<String>(portfolio, "name")));
		add(new TranscriptionDataView("transcriptions"));

	}

	@Override
	public String getPageTitle() {
		return (portfolio == null ? "" : portfolio.getName());
	}

	private class TranscriptionDataView extends GridView<Transcription> {

		public TranscriptionDataView(String id) {
			super(id, new ListDataProvider<Transcription>(transcriptions));
			setColumns(5);
		}

		@Override
		protected void populateItem(Item<Transcription> item) {
			item.add(new TranscriptionPanel("transcriptionPanel", item.getModel()));
		}

		@Override
		protected void populateEmptyItem(Item<Transcription> item) {
			item.add(new Label("transcriptionPanel", ""));
		}

	}
	
	private class TranscriptionPanel extends Panel {

		public TranscriptionPanel(String id, IModel<Transcription> model) {
			super(id, model);
			Transcription transcription = model.getObject();
			
			BookmarkablePageLink<TranscriptionPage> link = TranscriptionPage.getLink("transcriptionLink", repository, portfolio, transcription);
			link.add(new FacsimileImage("facsimileThumb", transcription, FacsimileResolution.THUMB));
			add(link);
			
			add(new Label("transcriptionName", new PropertyModel<String>(transcription, "name")));
		}
	}
}
