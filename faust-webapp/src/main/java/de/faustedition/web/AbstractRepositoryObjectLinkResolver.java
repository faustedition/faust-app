package de.faustedition.web;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import de.faustedition.model.repository.RepositoryObject;

public abstract class AbstractRepositoryObjectLinkResolver implements RepositoryObjectLinkResolver {

	@Override
	public BookmarkablePageLink<? extends Page> resolve(String id, RepositoryObject repositoryObject) {
		return resolve(id, repositoryObject.getClass(), repositoryObject.getPath());
	}
}
