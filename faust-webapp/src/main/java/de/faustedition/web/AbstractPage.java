package de.faustedition.web;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import de.faustedition.web.manuscripts.ManuscriptsPage;
import de.faustedition.web.project.AboutPage;
import de.faustedition.web.project.ContactPage;
import de.faustedition.web.project.ImprintPage;

@StatelessComponent
public abstract class AbstractPage extends WebPage {

	public AbstractPage() {
		setStatelessHint(true);
		add(CSSPackageResource.getHeaderContribution(AbstractPage.class, "FaustApplication.css", "screen"));
		add(JavascriptPackageResource.getHeaderContribution(AbstractPage.class, "FaustApplication.js"));
		
		IModel<String> titleModel = new PropertyModel<String>(this, "pageTitle");
		add(new Label("headTitle", titleModel));
		add(new Label("headerTitle", titleModel));
		add(new ResourceLink<ResourceReference>("grantApplicationLink", new ResourceReference(AboutPage.class, "FaustGrantApplication.pdf")) {
			@Override
			protected boolean getStatelessHint() {
				return true;
			}
		});
		add(new BookmarkablePageLink<ManuscriptsPage>("manuscriptsLink", ManuscriptsPage.class));
		add(new BookmarkablePageLink<AboutPage>("aboutLink", AboutPage.class));
		add(new BookmarkablePageLink<ContactPage>("contactLink", ContactPage.class));
		add(new BookmarkablePageLink<ImprintPage>("imprintLink", ImprintPage.class));
	}
	
	
	public abstract String getPageTitle();
}
