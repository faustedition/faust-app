package de.faustedition.web.manuscripts;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.springframework.util.Assert;

import de.faustedition.model.facsimile.FacsimileResolution;
import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.transcription.Portfolio;
import de.faustedition.model.transcription.Repository;
import de.faustedition.model.transcription.Transcription;
import de.faustedition.model.transcription.Manuscripts;
import de.faustedition.web.AbstractPage;
import de.faustedition.web.AbstractRepositoryObjectLinkResolver;
import de.faustedition.web.FaustApplication;
import de.faustedition.web.RepositoryObjectLinkResolver;
import de.faustedition.web.dav.DavResourceLink;
import de.faustedition.web.facsimile.FacsimileImage;

public class TranscriptionPage extends AbstractPage {

	public static final RepositoryObjectLinkResolver LINK_RESOLVER = new AbstractRepositoryObjectLinkResolver() {

		@Override
		public BookmarkablePageLink<? extends Page> resolve(String id, Class<? extends RepositoryObject> type, String path) {
			Assert.isAssignable(Transcription.class, type);
			String[] pathComponents = DataRepository.splitPath(path);
			PageParameters parameters = new PageParameters();
			parameters.add("0", pathComponents[pathComponents.length - 3]);
			parameters.add("1", pathComponents[pathComponents.length - 2]);
			parameters.add("2", pathComponents[pathComponents.length - 1]);
			return new BookmarkablePageLink<TranscriptionPage>(id, TranscriptionPage.class, parameters);
		}
	};
	
	private Repository repository;
	private Portfolio portfolio;
	private Transcription transcription;

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
				repository = Manuscripts.get(session).get(session, Repository.class, repositoryName);
				portfolio = repository.get(session, Portfolio.class, portfolioName);
				transcription = portfolio.get(session, Transcription.class, transcriptionName);
				return null;
			}
		});

		add(new Label("transcriptionHeader", new PropertyModel<String>(transcription, "name")));
		add(new FacsimileImage("facsimile", transcription, FacsimileResolution.LOW));
		add(new DavResourceLink("davLink", transcription));
	}

	@Override
	public String getPageTitle() {
		return (transcription == null ? "" : transcription.getName());
	}

}
