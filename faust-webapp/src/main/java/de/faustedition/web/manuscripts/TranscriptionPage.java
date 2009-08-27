package de.faustedition.web.manuscripts;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.InvalidUrlException;

import de.faustedition.model.facsimile.FacsimileResolution;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.facsimile.FacsimileImage;

public class TranscriptionPage extends AbstractPage {

	private Repository repository;
	private Portfolio portfolio;
	private Transcription transcription;

	public static BookmarkablePageLink<TranscriptionPage> getLink(String id, Repository repository, Portfolio portfolio, Transcription transcription) {
		PageParameters parameters = new PageParameters();
		parameters.add("0", repository.getName());
		parameters.add("1", portfolio.getName());
		parameters.add("2", transcription.getName());
		return new BookmarkablePageLink<TranscriptionPage>(id, TranscriptionPage.class, parameters);
	}

	public TranscriptionPage(PageParameters parameters) {
		super();

		final String repositoryName = parameters.getString("0");
		final String portfolioName = parameters.getString("1");
		final String transcriptionName = parameters.getString("2");
		if (repositoryName == null || portfolioName == null || transcriptionName == null) {
			throw new InvalidUrlException();
		}

		FaustApplication.get().accessDataRepository(new DataRepositoryTemplate<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				repository = TranscriptionStore.get(session).get(session, Repository.class, repositoryName);
				portfolio = repository.get(session, Portfolio.class, portfolioName);
				transcription = portfolio.get(session, Transcription.class, transcriptionName);
				return null;
			}
		});

		add(new Label("transcriptionHeader", new PropertyModel<String>(transcription, "name")));
		add(new FacsimileImage("facsimile", transcription, FacsimileResolution.LOW));
	}

	@Override
	public String getPageTitle() {
		return (transcription == null ? "" : transcription.getName());
	}

}
