package de.faustedition.web.dav;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.link.ExternalLink;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.RepositoryObject;

public class DavResourceLink extends ExternalLink {

	public DavResourceLink(String id, RepositoryObject linkTo) {
		super(id, createDavServletReference(linkTo));
		setContextRelative(true);
	}

	private static String createDavServletReference(RepositoryObject linkTo) {
		return StringUtils.join(new String[] { DAVServlet.URL_PREFIX, DataRepository.WORKSPACE, linkTo.getPath() }, "/");
	}

}
