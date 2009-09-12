package de.faustedition.web;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.faustedition.util.LoggingUtil;
import de.faustedition.web.genesis.GenesisPage;
import de.faustedition.web.manuscript.ManuscriptPage;
import de.faustedition.web.manuscript.ManuscriptsPage;
import de.faustedition.web.manuscript.PortfolioPage;
import de.faustedition.web.manuscript.RepositoryPage;
import de.faustedition.web.project.AboutPage;
import de.faustedition.web.project.ContactPage;
import de.faustedition.web.project.ImprintPage;
import de.faustedition.web.search.SearchPage;
import de.faustedition.web.text.TextPage;

public class FaustApplication extends WebApplication {
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

		mount(new IndexedParamUrlCodingStrategy("/search", SearchPage.class));

		mount(new IndexedParamUrlCodingStrategy("/manuscripts/repository", RepositoryPage.class));
		mount(new IndexedParamUrlCodingStrategy("/manuscripts/portfolio", PortfolioPage.class));
		mount(new IndexedParamUrlCodingStrategy("/manuscripts/transcription", ManuscriptPage.class));
	}

	@Override
	public RequestCycle newRequestCycle(Request request, Response response) {
		return new TransactionalRequestCycle(this, (WebRequest) request, (WebResponse) response);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return AboutPage.class;
	}

	public static WicketRuntimeException fatalError(String message, RuntimeException e) {
		LoggingUtil.LOG.fatal(message, e);
		return new WicketRuntimeException(message, e);
	}

	public static FaustApplication get() {
		return (FaustApplication) WebApplication.get();
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

	public static void assertFound(Object foundObject) {
		if (foundObject == null) {
			throw new AbortWithWebErrorCodeException(404);
		}
	}

	public static class TransactionalRequestCycle extends WebRequestCycle {
		private static final Set<String> READ_ONLY_METHODS = new HashSet<String>(Arrays.asList(new String[] { "GET", "HEAD"} ));
		
		@SpringBean
		private PlatformTransactionManager transactionManager;
		private TransactionStatus transactionStatus;
		private DefaultTransactionDefinition transactionDefinition;

		public TransactionalRequestCycle(WebApplication application, WebRequest request, Response response) {
			super(application, request, response);
			InjectorHolder.getInjector().inject(this);
			
			transactionDefinition = new DefaultTransactionDefinition();
			transactionDefinition.setReadOnly(READ_ONLY_METHODS.contains(request.getHttpServletRequest().getMethod()));
		}

		@Override
		protected void onBeginRequest() {
			transactionStatus = transactionManager.getTransaction(transactionDefinition);
			super.onBeginRequest();
		}

		@Override
		protected void onEndRequest() {
			super.onEndRequest();
			transactionManager.commit(transactionStatus);
		}

		@Override
		public Page onRuntimeException(Page page, RuntimeException e) {
			if (transactionStatus != null) {
				transactionManager.rollback(transactionStatus);
			}

			return super.onRuntimeException(page, e);
		}
	}
}
