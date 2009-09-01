package de.faustedition.web;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import de.faustedition.model.repository.RepositoryObject;

public interface RepositoryObjectLinkResolver {
	BookmarkablePageLink<? extends Page> resolve(String id, RepositoryObject repositoryObject);
	
	BookmarkablePageLink<? extends Page> resolve(String id, Class<? extends RepositoryObject> type, String path);
}
