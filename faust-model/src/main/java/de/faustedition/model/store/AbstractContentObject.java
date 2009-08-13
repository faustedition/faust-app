package de.faustedition.model.store;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class AbstractContentObject implements ContentObject {

	protected String path;
	protected String name;

	protected AbstractContentObject(String path, String name) {
		assert ContentStoreUtil.isValidName(name);
		this.path = ContentStoreUtil.normalizePath(path);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	public Node getNode(Session session) throws RepositoryException {
		return session.getRootNode().getNode(getPath());
	}

	public String buildRelativePath(String path) {
		return StringUtils.removeStart(path, getPath() + "/");
	}

	public String buildAbsolutePath(String path) {
		return (getPath() + "/" + path);
	}

	@Override
	public int compareTo(ContentObject o) {
		return ContentStoreUtil.compare(this, o);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", getPath()).toString();
	}
}
