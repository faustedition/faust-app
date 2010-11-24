package de.faustedition.document;

import org.restlet.routing.Router;
import org.restlet.routing.Template;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.template.TemplateFinder;

@Singleton
public class DocumentRouter extends Router {

	@Inject
	public DocumentRouter(TemplateFinder templateFinder, DocumentFinder documentFinder) {
		attach("styles", templateFinder);
		attach(documentFinder, Template.MODE_STARTS_WITH);
	}
}
