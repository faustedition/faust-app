package de.faustedition.document;

import de.faustedition.template.TemplateFinder;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocumentRouter extends Router implements InitializingBean {

	@Autowired
	private TemplateFinder templateFinder;

	@Autowired
	private DocumentImageLinkFinder imageLinkFinder;

	@Autowired
	private DocumentFinder documentFinder;

	@Override
	public void afterPropertiesSet() throws Exception {
		attach("styles", templateFinder);
		attach("imagelink", imageLinkFinder, Template.MODE_STARTS_WITH);
		attach(documentFinder, Template.MODE_STARTS_WITH);
	}
}
