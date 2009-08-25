package de.faustedition.web;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import de.faustedition.web.genesis.GenesisPage;
import de.faustedition.web.manuscripts.ManuscriptsPage;
import de.faustedition.web.project.AboutPage;
import de.faustedition.web.project.ContactPage;
import de.faustedition.web.project.ImprintPage;
import de.faustedition.web.text.TextPage;

@StatelessComponent
public abstract class AbstractPage extends WebPage {

	public AbstractPage() {
		setStatelessHint(true);
		add(CSSPackageResource.getHeaderContribution(AbstractPage.class, "FaustApplication.css", "screen"));
		add(JavascriptPackageResource.getHeaderContribution(AbstractPage.class, "FaustApplication.js"));

		add(new Label("headTitle", new PropertyModel<String>(this, "prefixedPageTitle")));
		add(new Label("headerTitle", new PropertyModel<String>(this, "pageTitle")));
		
		add(FaustApplication.get().hasRole("ROLE_EDITOR") ? new PrivateMainMenu("mainMenu") : new PublicMainMenu("mainMenu"));
	}

	public String getPrefixedPageTitle() {
		return "faustedition.net :: " + getPageTitle();
	}
	
	public abstract String getPageTitle();

	private abstract class MainMenu extends Panel {

		public MainMenu(String id) {
			super(id);
			add(new ResourceLink<ResourceReference>("grantApplicationLink", new ResourceReference(AboutPage.class, "FaustGrantApplication.pdf")) {
				@Override
				protected boolean getStatelessHint() {
					return true;
				}
			});
			add(new BookmarkablePageLink<AboutPage>("aboutLink", AboutPage.class));
			add(new BookmarkablePageLink<ContactPage>("contactLink", ContactPage.class));
			add(new BookmarkablePageLink<ImprintPage>("imprintLink", ImprintPage.class));
		}
	}

	private class PublicMainMenu extends MainMenu {

		public PublicMainMenu(String id) {
			super(id);
			add(new BookmarkablePageLink<LoginPage>("loginLink", LoginPage.class));
		}

	}

	private class PrivateMainMenu extends MainMenu {

		public PrivateMainMenu(String id) {
			super(id);
			add(new BookmarkablePageLink<ManuscriptsPage>("manuscriptsLink", ManuscriptsPage.class));
			add(new BookmarkablePageLink<TextPage>("textLink", TextPage.class));
			add(new BookmarkablePageLink<GenesisPage>("genesisLink", GenesisPage.class));
		}
	}
}
