package de.faustedition.web;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;
import de.faustedition.util.LoggingUtil;
import de.faustedition.web.genesis.GenesisPage;
import de.faustedition.web.manuscripts.ManuscriptsPage;
import de.faustedition.web.manuscripts.PortfolioPage;
import de.faustedition.web.manuscripts.RepositoryPage;
import de.faustedition.web.manuscripts.TranscriptionPage;
import de.faustedition.web.project.AboutPage;
import de.faustedition.web.project.ContactPage;
import de.faustedition.web.project.ImprintPage;
import de.faustedition.web.text.TextPage;

public class FaustApplication extends WebApplication {

	@SpringBean
	private DataRepository dataRepository;

	@Override
	protected void init() {
		super.init();

		addComponentInstantiationListener(new SpringComponentInjector(this));
		InjectorHolder.getInjector().inject(this);

		addPreComponentOnBeforeRenderListener(new StatelessChecker());

		mountBookmarkablePage("/project/about", AboutPage.class);
		mountBookmarkablePage("/project/contact", ContactPage.class);
		mountBookmarkablePage("/project/imprint", ImprintPage.class);
		mountBookmarkablePage("/manuscripts/", ManuscriptsPage.class);
		mountBookmarkablePage("/text/", TextPage.class);
		mountBookmarkablePage("/genesis/", GenesisPage.class);
		mountBookmarkablePage("/login", LoginPage.class);
		mount(new IndexedParamUrlCodingStrategy("/manuscripts/repository", RepositoryPage.class));
		mount(new IndexedParamUrlCodingStrategy("/manuscripts/portfolio", PortfolioPage.class));
		mount(new IndexedParamUrlCodingStrategy("/manuscripts/transcription", TranscriptionPage.class));
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return AboutPage.class;
	}

	public static WicketRuntimeException fatalError(String message, RepositoryException e) {
		LoggingUtil.LOG.fatal(message, e);
		return new WicketRuntimeException(message, e);
	}

	public static FaustApplication get() {
		return (FaustApplication) WebApplication.get();
	}

	public <T> T accessDataRepository(DataRepositoryTemplate<T> callback) {
		try {
			return dataRepository.execute(callback);
		} catch (PathNotFoundException e) {
			throw new AbortWithWebErrorCodeException(404);
		} catch (RepositoryException e) {
			throw fatalError("Error accessing content repository", e);
		}
	}

	public boolean hasRole(String role) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				if (role.equals(authority.getAuthority())) {
					return true;
				}
			}
		}
		return false;
	}
}
