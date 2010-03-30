package de.faustedition.document;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

public abstract class DocumentObject {
	protected DocumentObject parent;
	private Set<DocumentObject> children;

	@SuppressWarnings("unchecked")
	protected <T extends DocumentObject> T getAncestorOfType(Class<T> type) {
		DocumentObject current = parent;
		while (current != null) {
			if (type.isAssignableFrom(current.getClass())) {
				return (T) current;
			}
			current = current.parent;
		}
		return null;
	}

	public void add(DocumentObject child) {
		if (children == null) {
			children = new HashSet<DocumentObject>();
		}
		child.parent = this;
		children.add(child);
	}

	public void paint(Graphics2D g) {
		doPaint(g);

		if (children != null) {
			for (DocumentObject child : children) {
				child.paint(g);
			}
		}
	}
	
	protected abstract void doPaint(Graphics2D g);
}
